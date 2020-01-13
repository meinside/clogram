(ns meinside.cljs.test-runner
  (:require [cljs.nodejs :as nodejs]
            [cljs.test :refer [run-tests]]
            [meinside.cljs.clogram-test]))

;; nodejs doesn't have XMLHttpRequest...
(set! js/XMLHttpRequest (nodejs/require "xhr2"))

;; for printing console.print on nodejs
(enable-console-print!)

;; run tests
(run-tests 'meinside.cljs.clogram-test)
