(defproject clogram "0.0.1"
  :description "A Telegram Bot Library for Clojure"
  :url "https://github.com/meinside/clogram"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-http "3.10.0"]
                 [org.clojure/data.json "0.2.7"]]
  :repl-options {:init-ns clogram.bot})
