# clogram

A Clojure library for Telegram Bot API.

## Installation

Add `[dev.meinside/clogram "0.0.1"]` to the dependency of your project.clj file.

## Usage

### Interactively

```
(require '[meinside.clogram :as cg])

;; generate your bot token with this guide: https://core.telegram.org/bots#3-how-do-i-create-a-bot
(def token "0123456789:abcdefghijklmnopqrstuvwxyz")

;; create a new bot
(def bot (cg/new-bot token
                     :verbose? true))

;; get updates from your bot
(cg/get-update bot)

;; send 'typing...' to chat id: 123456
(cg/send-chat-action bot 123456 :typing)

;; send a message to chat id: 123456
(cg/send-message bot 123456 "this is a message from bot")
```

### Long-Polling

#### Sample Application (Echo)

```
;; clogram-sample/src/core.clj
;;
;; run with: $ lein run -m clogram-sample.core

(ns clogram-sample.core
  (:gen-class)
  (:require [clojure.core.async :as a]
            [meinside.clogram :as cg]))

(def token "0123456789:abcdefghijklmnopqrstuvwxyz")
(def interval 1)
(def verbose? false)
;(def verbose? true)
(def my-bot (cg/new-bot token :verbose? verbose?))

(defn echo
  "echo function"
  [bot update]
  (do
    (println ">>> received update:" update)

    (let [chat-id (get-in update [:message :chat :id])
          reply-to (get-in update [:message :message_id])
          text (get-in update [:message :text])]
      ;; 'typing...'
      (let [result (cg/send-chat-action bot chat-id :typing)]
        (if (not (:ok result))
          (println "*** failed to send chat action:" (:reason-pharse result))))

      (if (= text "/terminate")
        ;; process /terminate command
        (do
          (println ">>> received: /terminate")

          (cg/stop-polling bot)) ;; stop polling

        ;; or other texts
        (do
          ;; and reply to the received message
          (let [echoed-text (str "echo: " text)
                result (cg/send-message bot chat-id echoed-text
                         :reply-to-message-id reply-to)]
            (if (not (:ok result))
              (println "*** failed to send message:" (:reason-phrase result)))))))))

(defn -main
  "main function "
  [& args]
  (do
    (println ">>> launching application...")

    (let [wait (cg/poll-updates my-bot interval echo)]
      ;; add shutdown hook
      (.addShutdownHook (Runtime/getRuntime)
                        (Thread. #(do
                                    (println ">>> terminating application...")

                                    (cg/stop-polling my-bot)))) ;; stop polling

      ;; busy-wait
      (a/<!! wait))))

```

### Using Webhook

TODO - Add guides here.

## Todo

- [x] Add functions for long-polling updates.
- [ ] Add tests.
- [ ] Add functions for webhook.

## License

MIT

