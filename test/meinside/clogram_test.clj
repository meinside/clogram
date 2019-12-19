;;;; test/meinside/clogram_test.clj
;;;;
;;;; Test with:
;;;;
;;;; ```bash
;;;; $ TOKEN=xxxxx CHAT_ID=yyyyy lein test
;;;; ```

(ns meinside.clogram-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [meinside.clogram :as cg]))

;; fake tokens and chat id
(def test-bot-token "0123456789:abcdefghijklmnopqrstuvwxyz")
(def test-chat-id -1)
(def verbose? false) ;; set to true for printing verbose logs

;; initialize values from environment variables
(def bot (cg/new-bot (or (System/getenv "TOKEN")
                         test-bot-token)
                     :verbose? verbose?))
(def chat-id (or (System/getenv "CHAT_ID")
                 test-chat-id))

(deftest bot-creation-test
  (testing "Testing bot creation"
    (let [bot-info (cg/get-me bot)]
      (is (:ok bot-info)))))

(deftest sending-and-fetching-messages-test
  (testing "Testing sending and fetching messages"
    ;; delete webhook,
    (is (:ok (cg/delete-webhook bot)))

    ;; send a chat action,
    (is (:ok (cg/send-chat-action bot chat-id :typing)))

    ;; send a text message,
    (let [sent-message (cg/send-message bot chat-id "test message")]
      (is (:ok sent-message))

      ;; edit the message's text,
      (is (:ok (cg/edit-message-text bot "edited message"
                                     :chat-id chat-id
                                     :message-id (get-in sent-message [:result :message-id]))))

      ;; and forward it
      (is (:ok (cg/forward-message bot chat-id chat-id (get-in sent-message [:result :message-id])))))

    ;; send a photo,
    (let [sent-photo (cg/send-photo bot chat-id (io/file "resources/test/image.png"))]
      (is (:ok sent-photo))

      ;; edit the photo's caption
      (is (:ok (cg/edit-message-caption bot "caption"
                                        :chat-id chat-id
                                        :message-id (get-in sent-photo [:result :message-id])))))

    ;; TODO: send-audio

    ;; send a document,
    (let [sent-document (cg/send-document bot chat-id (io/file "test/meinside/clogram_test.clj"))]
      (is (:ok sent-document))

      ;; delete a message,
      (is (:ok (cg/delete-message bot chat-id (get-in sent-document [:result :message-id])))))

    ;; TODO: send-sticker

    ;; TODO: send-video

    ;; TODO: send-animation

    ;; TODO: send-voice

    ;; TODO: send-video-note

    ;; TODO: send-media-group

    ;; send a loation,
    (is (:ok (cg/send-location bot chat-id 37.5665 126.9780)))

    ;; TODO: send-venue

    ;; send a contact,
    (is (:ok (cg/send-contact bot chat-id "911" "Nine-One-One")))

    ;; send a poll,
    (let [sent-poll (cg/send-poll bot chat-id "The earth is...?" ["flat" "round" "nothing"])]
      (is (:ok sent-poll))

      ;; stop a poll,
      (cg/stop-poll bot chat-id (get-in sent-poll [:result :message-id])))

    ;; TODO: get-file-url

    ;; TODO: get-file

    ;; TODO: edit-message-media

    ;; TODO: edit-message-reply-markup

    ;; TODO: edit-message-live-location

    ;; TODO: stop-message-live-location

    ;; fetch messages
    (is (:ok (cg/get-updates bot)))))

(deftest polling-test
  (testing "Testing polling updates"
    ;; TODO: poll-updates

    ;; TODO: stop-polling-updates
    ))

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
    ;; TODO: kick-chat-member

    ;; TODO: leave-chat

    ;; TODO: unban-chat-member

    ;; TODO: restrict-chat-member

    ;; TODO: promote-chat-member

    ;; TODO: set-chat-permission

    ;; TODO: export-chat-invite-link

    ;; TODO: set-chat-photo

    ;; TODO: delete-chat-photo

    ;; TODO: set-chat-title

    ;; TODO: set-chat-description

    ;; TODO: pin-chat-message

    ;; TODO: unpin-chat-message

    ;; TODO: get-chat

    ;; TODO: get-user-profile-photos

    ;; TODO: get-chat-administrators

    ;; TODO: get-chat-members-count

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
