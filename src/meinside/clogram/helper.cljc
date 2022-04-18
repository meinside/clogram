;;;; Helper functions for Clogram
;;;;
;;;; src/meinside/clogram/helper.cljc
;;;;
;;;; created on : 2019.12.09.
;;;; last update: 2022.04.18.

(ns meinside.clogram.helper
  #?(:cljs
     (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require #?@(:clj [[clj-http.client :as http]
                      [clojure.data.json :as json]]
                :cljs [[cljs-http.client :as http]
                       [cljs.core.async :refer [<!]]
                       [clojure.walk :refer [postwalk]]])
            clojure.string))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; constants

(def api-baseurl "https://api.telegram.org/bot")
(def file-baseurl "https://api.telegram.org/file/bot")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; helper functions

;; print verbose log messages
(defn verbose
  [bot & args]
  (when (:verbose? bot)
    (#?(:clj println
        :cljs js/console.log)
             (str #?(:clj (java.time.LocalDateTime/now)
                     :cljs (.getTime (js/Date.))))
             "| VERBOSE |"
             (clojure.string/join "" (map #(if (coll? %) (pr-str %) (str %)) args)))))

;; print log messages
(defn log
  [& args]
  (#?(:clj println
      :cljs js/console.log)
           (str #?(:clj (java.time.LocalDateTime/now)
                   :cljs (.getTime (js/Date.))))
           "| LOG |"
           (clojure.string/join "" (map #(if (coll? %) (pr-str %) (str %)) args))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; http request helper functions

(defn- is-file?
  "Check if given object is a file object."
  [obj]
  #?(:clj (= (class obj) java.io.File)
     :cljs (or
             ; when it is HTML input
             (.-files obj)
             ; when it is js/Blob
             (.-size obj))))

(defn- has-file?
  "Check if given params include any file object."
  [params]
  (not-empty (filter is-file? (vals params))))

(defn- purge-nil-params
  "Remove params with nil value"
  [params]
  (filter (fn [[_ v]] (some? v)) params))

(defn- convert-param
  "Convert given param for proper http request."
  [param]
  (cond
    (is-file? param) param
    (string? param) param
    (keyword? param) (name param)
    :else #?(:clj (json/write-str param)
             :cljs (.stringify js/JSON (clj->js param)))))

(defn- params-for-multipart
  "Convert given params for multipart data."
  [params]
  (map (fn [[k v]] {:name (str k)
                    :content (convert-param v)}) params))

(defn- params-for-urlencoded
  "Convert given params for urlencoded data."
  [params]
  (reduce (fn [kv [k v]]
            (assoc kv k (convert-param v))) {} params))

(defn- request-multipart
  "Send multipart form data."
  [bot url params]
  (let [timeout-msecs (* 1000 (:timeout-seconds bot))]
    (verbose bot "sending multipart form data to: " url ", params: " params)

    #?(:clj (http/post url {:multipart (params-for-multipart params)
                            :socket-timeout timeout-msecs
                            :connection-timeout timeout-msecs
                            :accept :json
                            :throw-exceptions false})
       :cljs (http/post url {:multipart-params (params-for-multipart params)
                             :timeout timeout-msecs}))))

(defn- request-urlencoded
  "Send urlencoded form data."
  [bot url params]
  (let [timeout-msecs (* 1000 (:timeout-seconds bot))]
    (verbose bot "sending urlencoded data to: " url ", params: " params)

    #?(:clj (http/post url {:form-params (params-for-urlencoded params)
                            :socket-timeout timeout-msecs
                            :connection-timeout timeout-msecs
                            :accept :json
                            :throw-exceptions false})
       :cljs (http/post url {:form-params (params-for-urlencoded params)
                             :with-credentials? false ; XXX - due to CORS
                             :timeout timeout-msecs}))))

#?(:clj (defn- key->keyword
          "Convert json key to clojure keyword."
          [key]
          (keyword (clojure.string/replace key "_" "-")))
   :cljs (defn keyword->kebabed
           "Convert given keyword to kebab-case."
           [k]
           ;; https://stackoverflow.com/questions/36723449/change-all-keywords-in-collection-removing-dots-from-the-namespace-and-replacin
           (when (keyword? k)
             (->> k
                  str
                  (map (some-fn {\_ \-} identity))
                  rest
                  clojure.string/join
                  keyword))))

(defn request
  "Send HTTP request with given method name and params.

  Return a response synchronously on Clojure,
  and return a response channel on ClojureScript.
  
  Keywords in returned responses are in kebab-case."
  [bot method params]
  (let [f (fn [b m ps]
            (let [token (:token b)
                  url (str api-baseurl token "/" m)
                  params (purge-nil-params ps)
                  result (if (has-file? params)
                           (request-multipart bot url params)
                           (request-urlencoded bot url params))]
              #?(:clj (cond
                        (= (:status result) 200) (json/read-str (:body result) :key-fn key->keyword)
                        :else (do 
                                (verbose bot "request error: " result)

                                (assoc result :ok false)))
                 :cljs (go (let [res (<! result)]
                             (cond
                               (= (:status res) 200) (postwalk (some-fn keyword->kebabed identity) (:body res))
                               :else (do 
                                       (verbose bot "request error: " res)

                                       (assoc res :ok false))))))))]
    #?(:clj (try (f bot method params)
                 (catch Exception e {:ok false
                                     :reason-phrase (.getMessage e)}))
       :cljs (f bot method params))))

(defn url-for-filepath
  "Generate a URL from a fetched file info.

  (https://core.telegram.org/bots/api#getfile)"
  [bot filepath]
  (str file-baseurl (:token bot) "/" filepath))

