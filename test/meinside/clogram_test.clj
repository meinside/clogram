;; clogram_test.clj
;;
;; NOTE:
;;
;; Group privacy mode of both bots should be turned off (https://core.telegram.org/bots#privacy-mode)
;; before adding to a group chat.
;;
;;
;; Test with:
;;
;; $ BOT_TOKEN1=xxxxx BOT_TOKEN2=yyyyy CHAT_ID=zzzzz lein test

(ns meinside.clogram-test
  (:require [clojure.test :refer :all]
            [meinside.clogram :refer :all]))

;; fake tokens and chat id
(def test-bot1-token "0123456789:abcdefghijklmnopqrstuvwxyz")
(def test-bot2-token "9876543210:zyxwvutsrqponmlkjihgfedcba")
(def test-chat-id -1)
(def verbose? false) ;; set to true for printing verbose logs

;; initialize bots from environment variables
(def bot1 (new-bot (or (System/getenv "BOT_TOKEN1")
                       test-bot1-token)
                   :verbose? verbose?))
(def bot2 (new-bot (or (System/getenv "BOT_TOKEN2")
                       test-bot2-token)
                   :verbose? verbose?))
(def chat-id (or (System/getenv "CHAT_ID")
                 test-chat-id))

(deftest test-bot-creation
  (testing "Testing bot creation"
    (let [bot1-info (get-me bot1)
          bot2-info (get-me bot2)]
      (is (:ok bot1-info))
      (is (:ok bot2-info)))))

(deftest test-sending-and-fetching-messages
  (testing "Testing sending and fetching messages"
    ;; delete webhooks
    (is (:ok (delete-webhook bot1)))
    (is (:ok (delete-webhook bot2)))

    ;; send text messages
    (is (:ok (send-message bot1 chat-id "test message from bot1")))
    (is (:ok (send-message bot2 chat-id "test message from bot2")))

    ;; TODO - forward-message

    ;; TODO - send-photo

    ;; TODO - send-audio

    ;; TODO - send-document

    ;; TODO - send-sticker

    ;; TODO - send-video

    ;; TODO - send-animation

    ;; TODO - send-voice

    ;; TODO - send-video-note

    ;; TODO - send-media-group

    ;; TODO - send-location

    ;; TODO - send-venue

    ;; TODO - send-contact

    ;; TODO - send-poll

    ;; TODO - stop-poll

    ;; TODO - send-chat-action

    ;; TODO - get-file-url

    ;; TODO - get-file

    ;; TODO - edit-message-text

    ;; TODO - edit-message-caption

    ;; TODO - edit-message-media

    ;; TODO - edit-message-reply-markup

    ;; TODO - edit-message-live-location

    ;; TODO - stop-message-live-location

    ;; TODO - delete-message

    ;; fetch messages
    (is (:ok (get-updates bot1)))
    (is (:ok (get-updates bot2)))))

(deftest test-polling
  (testing "Testing polling updates"
    ;; TODO - poll-updates

    ;; TODO - stop-polling
    ))

(deftest test-stickers
  (testing "Testing functions for stickers"
    ;; TODO - get-sticker-set

    ;; TODO - upload-sticker-file

    ;; TODO - create-new-sticker-set

    ;; TODO - add-sticker-to-set

    ;; TODO - set-sticker-position-in-set

    ;; TODO - delete-sticker-from-set
    ))

(deftest test-games
  (testing "Testing functions for games"
    ;; TODO - send-game

    ;; TODO - set-game-score

    ;; TODO - get-game-highscores
    ))

(deftest test-shopping
  (testing "Testing functions for commerce"
    ;; TODO - send-invoice

    ;; TODO - answer-shipping-query

    ;; TODO - answer-pre-checkout-query
    ))

(deftest test-chat-administration
  (testing "Testing functions for chat administration"
    ;; TODO - kick-chat-member

    ;; TODO - leave-chat

    ;; TODO - unban-chat-member

    ;; TODO - restrict-chat-member

    ;; TODO - promote-chat-member

    ;; TODO - set-chat-permission

    ;; TODO - export-chat-invite-link

    ;; TODO - set-chat-photo

    ;; TODO - delete-chat-photo

    ;; TODO - set-chat-title

    ;; TODO - set-chat-description

    ;; TODO - pin-chat-message

    ;; TODO - unpin-chat-message

    ;; TODO - get-chat

    ;; TODO - get-user-profile-photos

    ;; TODO - get-chat-administrators

    ;; TODO - get-chat-members-count

    ;; TODO - get-chat-member

    ;; TODO - set-chat-sticker-set

    ;; TODO - delete-chat-sticker-set
    ))

(deftest test-callback-query
  (testing "Testing callback query"
    ;; TODO - answer-callback-query
    ))

(deftest test-inline-query
  (testing "Testing inline query"
    ;; TODO - answer-inline-query
    ))

