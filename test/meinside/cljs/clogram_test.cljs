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
  (:require-macros
    [cljs.core.async.macros :as a :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
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
           (go
             ;; get my info
             (let [ch (cg/get-me bot)
                   res (<! ch)]
               (is (:ok res))

               ;; TODO: log-out

               ;; TODO: close-bot

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

             ;; get bot name
             (let [my-name (<! (cg/get-my-name bot))]
               (is (:ok my-name))

               ;; if bot name can be set,
               (if (not= (get-in my-name [:result :name]) "telegram api test bot")
                 ;; set bot name
                 (is (:ok (<! (cg/set-my-name bot "telegram api test bot"))))
                 nil)) ;; or do nothing

             ;; set bot description
             (is (:ok (<! (cg/set-my-description bot :description "A bot for testing library: clogram/cljs"))))

             ;; get bot description
             (is (:ok (<! (cg/get-my-description bot))))

             ;; set bot short description
             (is (:ok (<! (cg/set-my-short-description bot :short-description "clogram/cljs"))))

             ;; get bot short description
             (is (:ok (<! (cg/get-my-short-description bot))))

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

             ;; TODO: copy-messages

             ;; TODO: forward-messages

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
             (let [ch (cg/send-poll bot chat-id "The earth is...?" [{:text "flat"} {:text "round"} {:text "nothing"}])
                   sent-poll (<! ch)]
               (is (:ok sent-poll))

               ;; stop a poll,
               (is (:ok (<! (cg/stop-poll bot chat-id (get-in sent-poll [:result :message-id]))))))

             ;; TODO: get-file

             ;; TODO: delete-message

             ;; TODO: delete-messages

             ;; TODO: edit-message-caption

             ;; TODO: edit-message-media

             ;; TODO: edit-message-reply-markup

             ;; TODO: edit-message-live-location

             ;; TODO: stop-message-live-location

             ;; TODO: set-message-reaction

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
  (testing "Testing functions for stickers"))
;; TODO: get-sticker-set

;; TODO: get-custom-emoji-stickers

;; TODO: upload-sticker-file

;; TODO: create-new-sticker-set

;; TODO: add-sticker-to-set

;; TODO: set-sticker-position-in-set

;; TODO: delete-sticker-from-set

;; TODO: replace-sticker-in-set

;; TODO: set-sticker-set-thumbnail

;; TODO: set-custom-emoji-sticker-set-thumbnail

;; TODO: set-sticker-set-title

;; TODO: delete-sticker-set

;; TODO: set-sticker-emoji-list

;; TODO: set-sticker-keywords

;; TODO: set-sticker-mask-position

(deftest games-test
  (testing "Testing functions for games"))
;; TODO: send-game

;; TODO: set-game-score

;; TODO: get-game-highscores

(deftest shopping-test
  (testing "Testing functions for commerce"))
;; TODO: send-invoice

;; TODO: create-invoice-link

;; TODO: answer-shipping-query

;; TODO: answer-pre-checkout-query

(deftest chat-administration-test
  (testing "Testing functions for chat administration"
    (async done
           (go
             ;; TODO: ban-chat-member

             ;; TODO: leave-chat

             ;; TODO: unban-chat-member

             ;; TODO: restrict-chat-member

             ;; TODO: promote-chat-member

             ;; TODO: set-chat-administrator-custom-title

             ;; TODO: ban-chat-sender-chat

             ;; TODO: unban-chat-sender-chat

             ;; TODO: set-chat-permissions

             ;; TODO: export-chat-invite-link

             ;; TODO: create-chat-invite-link

             ;; TODO: edit-chat-invite-link

             ;; TODO: revoke-chat-invite-link

             ;; TODO: approve-chat-join-request

             ;; TODO: decline-chat-join-request

             ;; TODO: set-chat-photo

             ;; TODO: delete-chat-photo

             ;; TODO: set-chat-title

             ;; set-chat-description
             (is (:ok (<! (cg/set-chat-description bot chat-id (str "[clogram/cljs] chat_id: " chat-id " (last update: " (.getTime (js/Date.)) ")")))))

             ;; TODO: pin-chat-message

             ;; TODO: unpin-chat-message

             ;; TODO: unpin-all-chat-messages

             ;; TODO: get-chat

             ;; TODO: get-user-profile-photos

             ;; TODO: get-user-chat-boosts

             ;; TODO: get-business-connection

             ;; TODO: get-chat-administrators

             ;; TODO: get-chat-member-count

             ;; TODO: get-chat-member

             ;; TODO: set-chat-sticker-set

             ;; TODO: delete-chat-sticker-set

             ;; TODO: get-forum-topic-icon-stickers

             ;; TODO: set-chat-menu-button

             ;; TODO: get-chat-menu-button

             ;; TODO: set-my-default-administrator-rights

             ;; TODO: get-my-default-administrator-rights

             ;; TODO: create-forum-topic

             ;; TODO: edit-forum-topic

             ;; TODO: close-forum-topic

             ;; TODO: reopen-forum-topic

             ;; TODO: delete-forum-topic

             ;; TODO: unpin-all-forum-topic-messages

             ;; TODO: edit-general-forum-topic

             ;; TODO: close-general-forum-topic

             ;; TODO: reopen-general-forum-topic

             ;; TODO: hide-general-forum-topic

             ;; TODO: unhide-general-forum-topic

             ;; TODO: unpin-all-general-forum-topic-messages

             (done)))))

(deftest callback-query-test
  (testing "Testing callback query"))
;; TODO: answer-callback-query

(deftest inline-query-test
  (testing "Testing inline query"))
;; TODO: answer-inline-query

(deftest web-app-query-test
  (testing "Testing web app query"))
;; TODO: answer-web-app-query

