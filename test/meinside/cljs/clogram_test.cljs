;;;; test/meinside/cljs/clogram_test.cljs
;;;;
;;;; Test with:
;;;;
;;;; ```bash
;;;; $ lein clean
;;;; $ lein npm install
;;;; $ TOKEN=xxxxx CHAT_ID=yyyyy lein cljsbuild test
;;;;
;;;; # for verbose messages:
;;;; $ TOKEN=xxxxx CHAT_ID=yyyyy VERBOSE=true lein cljsbuild test
;;;; ```

(ns meinside.cljs.clogram-test
  (:require-macros [cljs.core.async.macros :as a :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [cljs.test :refer-macros [deftest is testing async]]
            [meinside.clogram :as cg]))

(defn -js->clj+
  "For cases when built-in js->clj doesn't work. Source: https://stackoverflow.com/a/32583549/4839573"
  [x]
  (into {} (for [k (js-keys x)]
             [k (aget x k)])))

;; https://gist.github.com/metametadata/b67a3e7f722589e04b021d60510be504
(defn- read-env-var
  "Read an environment variable with given key."
  [key]
  (get (-js->clj+ (.-env js/process)) key))

;; fake tokens and chat id
(def test-bot-token "0123456789:abcdefghijklmnopqrstuvwxyz")
(def test-chat-id -1)
(def verbose? (= (read-env-var "VERBOSE") "true"))

;; initialize values from environment variables
(def bot (cg/new-bot (or (read-env-var "TOKEN")
                         test-bot-token)
                     :verbose? verbose?))
(def chat-id (or (read-env-var "CHAT_ID")
                 test-chat-id))

(deftest bot-creation-test
  (testing "Testing bot creation"
    (async done
           (go (let [ch (cg/get-me bot)
                     res (<! ch)]
                 (is (:ok res))
                 (done))))))

(deftest sending-and-fetching-messages-test
  (testing "Testing sending and fetching messages"
    (async done
           (go
             ;; delete webhook,
             (is (:ok (<! (cg/delete-webhook bot))))

             ;; delete bot commands
             (is (:ok (<! (cg/delete-my-commands bot))))

             ;; set bot commands
             (is (:ok (<! (cg/set-my-commands bot [{:command "/help" :description "show help messages"}]))))

             ;; get bot commands
             (is (:ok (<! (cg/get-my-commands bot))))

             ;; send a chat action,
             (is (:ok (<! (cg/send-chat-action bot chat-id :typing))))

             ;; send a text message,
             (let [ch (cg/send-message bot chat-id "test message")
                   sent-message (<! ch)]
               (is (:ok sent-message))

               ;; edit the message's text,
               (is (:ok (<! (cg/edit-message-text bot "edited message"
                                                  :chat-id chat-id
                                                  :message-id (get-in sent-message [:result :message-id])))))

               ;; copy it,
               (is (:ok (<! (cg/copy-message bot chat-id chat-id (get-in sent-message [:result :message-id])))))

               ;; and forward it
               (is (:ok (<! (cg/forward-message bot chat-id chat-id (get-in sent-message [:result :message-id]))))))

             ;; TODO: send-photo (XXX - Blob is not supported in nodejs)

             ;; TODO: send-audio

             ;; TODO: send-document (XXX - Blob is not supported in nodejs)

             ;; TODO: send-sticker

             ;; TODO: send-video

             ;; TODO: send-animation

             ;; TODO: send-voice

             ;; TODO: send-video-note

             ;; TODO: send-media-group

             ;; send a location,
             (is (:ok (<! (cg/send-location bot chat-id 37.5665 126.9780))))

             ;; TODO: send-venue

             ;; send a contact,
             (is (:ok (<! (cg/send-contact bot chat-id "911" "Nine-One-One"))))

             ;; send a poll,
             (let [ch (cg/send-poll bot chat-id "The earth is...?" ["flat" "round" "nothing"])
                   sent-poll (<! ch)]
               (is (:ok sent-poll))

               ;; stop a poll,
               (is (:ok (<! (cg/stop-poll bot chat-id (get-in sent-poll [:result :message-id]))))))

             ;; TODO: get-file-url

             ;; TODO: get-file

             ;; TODO: delete-message

             ;; TODO: edit-message-caption

             ;; TODO: edit-message-media

             ;; TODO: edit-message-reply-markup

             ;; TODO: edit-message-live-location

             ;; TODO: stop-message-live-location

             ;; fetch messages
             (is (:ok (<! (cg/get-updates bot))))

             (done)))))

(deftest polling-test
  (testing "Testing polling updates"
    (async done
           (go
             ;; try stopping polling before starting, (will fail and return false)
             (is (not (cg/stop-polling-updates bot)))

             ;; start polling updates,
             (is (cg/poll-updates bot 1 (fn [_ _] nil)))

             ;; sleep for a while,
             (js/setTimeout (fn []
                              ;; try polling after it is started, (will fail and return false)
                              (is (not (cg/poll-updates bot 1 (fn [_ _] nil))))

                              ;; sleep for a while again,
                              (js/setTimeout (fn []
                                               ;; then stop polling
                                               (is (cg/stop-polling-updates bot))

                                               (done))) 1000) 1000)))))

(deftest stickers-test
  (testing "Testing functions for stickers"
    ;; TODO: get-sticker-set

    ;; TODO: upload-sticker-file

    ;; TODO: create-new-sticker-set

    ;; TODO: add-sticker-to-set

    ;; TODO: set-sticker-position-in-set

    ;; TODO: delete-sticker-from-set
    ))

(deftest games-test
  (testing "Testing functions for games"
    ;; TODO: send-game

    ;; TODO: set-game-score

    ;; TODO: get-game-highscores
    ))

(deftest shopping-test
  (testing "Testing functions for commerce"
    ;; TODO: send-invoice

    ;; TODO: answer-shipping-query

    ;; TODO: answer-pre-checkout-query
    ))

(deftest chat-administration-test
  (testing "Testing functions for chat administration"
    ;; TODO: ban-chat-member

    ;; TODO: leave-chat

    ;; TODO: unban-chat-member

    ;; TODO: restrict-chat-member

    ;; TODO: promote-chat-member

    ;; TODO: set-chat-administrator-custom-title

    ;; TODO: set-chat-permission

    ;; TODO: export-chat-invite-link

    ;; TODO: set-chat-photo

    ;; TODO: delete-chat-photo

    ;; TODO: set-chat-title

    ;; TODO: set-chat-description

    ;; TODO: pin-chat-message

    ;; TODO: unpin-chat-message

    ;; TODO: unpin-all-chat-messages

    ;; TODO: get-chat

    ;; TODO: get-user-profile-photos

    ;; TODO: get-chat-administrators

    ;; TODO: get-chat-member-count

    ;; TODO: get-chat-member

    ;; TODO: set-chat-sticker-set

    ;; TODO: delete-chat-sticker-set
    ))

(deftest callback-query-test
  (testing "Testing callback query"
    ;; TODO: answer-callback-query
    ))

(deftest inline-query-test
  (testing "Testing inline query"
    ;; TODO: answer-inline-query
    ))
