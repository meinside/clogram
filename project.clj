(defproject dev.meinside/clogram "0.0.10"
  :description "A Telegram Bot Library for Clojure"
  :url "https://github.com/meinside/clogram"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "0.6.532"]
                 [org.clojure/data.json "0.2.7"]
                 [clj-http "3.10.0"]]
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :scm {:name "git" :url "https://github.com/meinside/clogram"}
  :repl-options {:init-ns meinside.clogram})