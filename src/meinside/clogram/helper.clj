;;;; Helper functions for Clogram
;;;;
;;;; src/meinside/clogram/helper.clj
;;;;
;;;; created on 2019.12.09.

(ns meinside.clogram.helper
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

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
  (if (:verbose? bot)
    (println (str (java.time.LocalDateTime/now)) "| VERBOSE |" (clojure.string/join "" (map #(if (coll? %) (pr-str %) (str %)) args)))))

;; print log messages
(defn log
  [& args]
  (println (str (java.time.LocalDateTime/now)) "| LOG |" (clojure.string/join "" (map #(if (coll? %) (pr-str %) (str %)) args))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; http request helper functions

(defn- is-file?
  "Check if given object is a file object"
  [obj]
  (= (class obj) java.io.File))

(defn- has-file?
  "Check if given params include any file object"
  [params]
  (not-empty (filter is-file? (vals params))))

(defn- purge-nil-params
  "Remove params with nil value"
  [params]
  (filter (fn [[_ v]] (some? v)) params))

(defn- convert-param
  "Convert given param for proper http request"
  [param]
  (cond
    (is-file? param) param
    (string? param) param
    (keyword? param) (name param)
    :else (json/write-str param)))

(defn- params-for-multipart
  "Convert given params for multipart data"
  [params]
  (map (fn [[k v]] {:name (str k)
                    :content (convert-param v)}) params))

(defn- params-for-urlencoded
  "Convert given params for urlencoded data"
  [params]
  (reduce (fn [kv [k v]]
            (assoc kv k (convert-param v))) {} params))

(defn- request-multipart
  "Send multipart form data"
  [bot url params]
  (let [timeout-msecs (* 1000 (:timeout-seconds bot))]
    (do
      (verbose bot "sending multipart form data to: " url ", params: " params)
      (http/post url {:multipart (params-for-multipart params)
                      :socket-timeout timeout-msecs
                      :connection-timeout timeout-msecs
                      :accept :json
                      :throw-exceptions false}))))

(defn- request-urlencoded
  "Send urlencoded form data"
  [bot url params]
  (let [timeout-msecs (* 1000 (:timeout-seconds bot))]
    (do
      (verbose bot "sending urlencoded data to: " url ", params: " params)
      (http/post url {:form-params (params-for-urlencoded params)
                      :socket-timeout timeout-msecs
                      :connection-timeout timeout-msecs
                      :accept :json
                      :throw-exceptions false}))))

(defn request
  "Send HTTP request with given method name and params"
  [bot method params]
  (try
    (let [token (:token bot)
          url (str api-baseurl token "/" method)
          params (purge-nil-params params)]
      (let [result (if (has-file? params)
                     (request-multipart bot url params)
                     (request-urlencoded bot url params))]
        (cond
          (= (:status result) 200) (json/read-str (:body result) :key-fn keyword)
          :else (assoc result :ok false))))
    (catch Exception e {:ok false
                        :reason-phrase (.getMessage e)})))

(defn url-for-filepath
  "Generate a URL from a fetched file info.

  (https://core.telegram.org/bots/api#getfile)"
  [bot filepath]
  (let [token (:token bot)]
    (str file-baseurl (:token bot) "/" filepath)))

