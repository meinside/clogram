(defproject dev.meinside/clogram "0.30.0"
  :description "A Telegram Bot Library for Clojure"
  :url "https://github.com/meinside/clogram"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}

  ;; dependencies
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.clojure/clojurescript "1.11.132" :exclusions [org.apache.ant/ant]]
                 [org.clojure/core.async "1.6.681"]
                 [org.clojure/data.json "2.5.0"]
                 [clj-http "3.13.0"]
                 [cljs-http "0.1.48"]]

  ;; paths
  :source-paths ["src"]
  :test-paths ["test"]

  ;; aliases
  :aliases {"test-cljs" ["cljsbuild" "test" "unit-tests"]
            "test-cljs-auto" ["cljsbuild" "auto" "tests"]}

  ;; for testing cljs
  ;; https://medium.com/@jamesleonis/step-inside-cljsbuild-e38271b10415
  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-npm "0.6.2" :hooks false]]
  :cljsbuild {:builds {:production {:source-paths ["src"]
                                    :compiler {:output-to "target/production.js"
                                               :optimizations :advanced}}
                       :tests {:source-paths ["src" "test"]
                               :compiler {:output-to "target/testable.js"
                                          :optimizations :none
                                          :target :nodejs
                                          :main meinside.cljs.test-runner}}}
              :test-commands {"unit-tests" ["node" "target/testable.js"]}}
  :clean-targets ^{:protect false} [:target-path "target"]
  :npm {:dependencies [[xhr2 "0.2.0"]]}

  ;; for deployment
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :scm {:name "git" :url "https://github.com/meinside/clogram"}

  ;; for REPL
  :repl-options {:init-ns meinside.clogram})
