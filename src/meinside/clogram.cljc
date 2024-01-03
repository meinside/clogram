;;;; Telegram Bot Library for Clojure
;;;;
;;;; src/meinside/clogram.cljc
;;;;
;;;; (https://core.telegram.org/bots/api)
;;;;
;;;; created on : 2019.12.05.
;;;; last update: 2024.01.03.

(ns meinside.clogram
  #?(:cljs (:require-macros [cljs.core.async.macros :as a :refer [go]]))
  (:require
    [meinside.clogram.helper :as h]
    #?(:clj [clojure.core.async :as a :refer [<! <!! go close!]]
       :cljs [cljs.core.async :refer [<! close! chan]]))) ; helper functions

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; constants

(def default-interval-seconds 1)
(def default-timeout-seconds 10)
(def default-limit-count 100)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; Bot

(defrecord Bot [token
                interval-seconds
                limit-count
                timeout-seconds
                polling?
                polling-wait-ch
                verbose?])

;; create a new bot with given params
(defn new-bot
  "Create a new bot with given token and options."
  [token & opts]
  (let [{:keys [interval-seconds
                limit-count
                timeout-seconds
                verbose?]
         :or {interval-seconds default-interval-seconds
              limit-count default-limit-count
              timeout-seconds default-timeout-seconds
              verbose? false}} opts]
    (map->Bot {:token token
               :interval-seconds interval-seconds
               :limit-count limit-count
               :timeout-seconds timeout-seconds
               :polling? (atom false)
               :polling-wait-ch (atom nil)
               :verbose? verbose?})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;
;;; bot API methods
;;;
;;; (https://core.telegram.org/bots/api#available-methods)

(defn delete-webhook
  "Delete webhook for polling messages.

  (https://core.telegram.org/bots/api#deletewebhook)"
  [bot & options]
  (let [{:keys [drop-pending-updates]} options]
    (h/request bot "deleteWebhook" {"drop_pending_updates" drop-pending-updates})))

(defn get-me
  "Fetch this bot's info.

  (https://core.telegram.org/bots/api#getme)"
  [bot]
  (h/request bot "getMe" {}))

(defn get-updates
  "Fetch updates for this bot.

  `options` include: :offset, :limit, :timeout, and :allowed-updates.

  (https://core.telegram.org/bots/api#getupdates)"
  [bot & options]
  (let [{:keys [offset
                limit
                timeout
                allowed-updates]
         :or {limit 100
              timeout (:timeout-seconds bot)}} options]
    (h/request bot "getUpdates" {"offset" offset
                                 "limit" limit
                                 "timeout" timeout
                                 "allowed_updates" allowed-updates})))

;; timeout function for ClojureScript
;; https://gist.github.com/swannodette/5882703
#?(:cljs
     (defn- cljs-timeout [ms]
       (let [c (chan)]
         (js/setTimeout (fn [] (close! c)) ms)
         c)))

(defn poll-updates
  "Poll updates for this bot with given interval and send them through the handler function.

  On Clojure, this function will block until it gets stopped with `stop-polling-updates` function.

  `options` include: :offset, :limit, :timeout, and :allowed-updates.

  Start polling with:

  ;; will be blocked until stopped
  (poll-updates bot 1 (fn [bot update] ...))

  and stop polling with:

  (stop-polling-updates bot)"
  [bot interval-seconds fn-update-handler & options]
  (let [polling? (:polling? bot)]
    (if @polling?
      (do
        ;; already polling, do nothing
        (h/log "already polling")

        ;; and return false
        false)

      ;; start polling
      (let [{:keys [offset
                    limit
                    timeout
                    allowed-updates]
             :or {limit 100
                  timeout (:timeout-seconds bot)}} options
            interval-seconds (max default-interval-seconds interval-seconds)
            update-offset (atom offset)
            polling-wait-ch (:polling-wait-ch bot)
            wait (go
                   (h/log "starting polling with interval: " interval-seconds " second(s)")

                   (reset! polling? true)

                   ;; keep polling...
                   (while @polling?
                     (let [response #?(:clj (get-updates bot
                                                         :offset @update-offset
                                                         :limit limit
                                                         :timeout timeout
                                                         :allowed-updates allowed-updates)
                                       :cljs (<! (get-updates bot
                                                              :offset @update-offset
                                                              :limit limit
                                                              :timeout timeout
                                                              :allowed-updates allowed-updates)))]
                       (if (:ok response)
                         (if (not-empty (:result response))
                           (do
                             ;; new update-offset = latest update-id + 1
                             (reset! update-offset (inc (last (sort (map :update-id (:result response))))))

                             ;; callback updates
                             (doseq [update (:result response)]
                               (go (fn-update-handler bot update))))
                           (h/log "no updates..."))
                         (h/log "failed to poll updates: " (:reason-phrase response)))

                       ;; interval
                       (<! (#?(:clj a/timeout
                               :cljs cljs-timeout) (* 1000 interval-seconds)))))

                   ;; out of while-loop
                   (h/log "stopped polling."))]

        ;; save channel,
        (reset! polling-wait-ch wait)

        ;; wait for it, (busy-waits only for Clojure, not ClojureScript)
        #?(:clj
             (<!! wait))

        ;; and return true (when finished on Clojure, or immediately on ClojureScript)
        true))))

(defn stop-polling-updates
  "Stop polling updates if the bot was polling.

  Returns true on success, false otherwise."
  [bot]
  (let [polling? (:polling? bot)
        wait (:polling-wait-ch bot)]
    (if (and @polling? @wait)
      ;; if polling,
      (do
        (h/log "stopping polling updates...")

        ;; make it false
        (reset! polling? false)

        ;; close channel and make it nil
        (close! @wait)
        (reset! wait nil)

        ;; return true
        true)

      ;; if not polling,
      (do
        (h/log "not polling (anymore)")

        ;; return false
        false))))

(defn send-message
  "Send a message.

  `options` include: :message-thread-id, :parse-mode, :entities, :link-preview-options, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendmessage)"
  [bot chat-id text & options]
  (let [{:keys [message-thread-id
                parse-mode
                entities
                link-preview-options
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendMessage" {"chat_id" chat-id
                                  "message_thread_id" message-thread-id
                                  "text" text
                                  "parse_mode" parse-mode
                                  "entities" entities
                                  "link_preview_options" link-preview-options
                                  "disable_notification" disable-notification
                                  "protect_content" protect-content
                                  "reply_parameters" reply-parameters
                                  "reply_markup" reply-markup})))

(defn forward-message
  "Forward a message.

  `options` include: :message-thread-id, :disable-notification, and :protect-content.

  (https://core.telegram.org/bots/api#forwardmessage)"
  [bot chat-id from-chat-id message-id & options]
  (let [{:keys [message-thread-id
                disable-notification
                protect-content]} options]
    (h/request bot "forwardMessage" {"chat_id" chat-id
                                     "message_thread_id" message-thread-id
                                     "from_chat_id" from-chat-id
                                     "message_id" message-id
                                     "disable_notification" disable-notification
                                     "protect_content" protect-content})))

(defn forward-messages
  "Forward messages.

  `options` include: :message-thread-id, :disable-notification, and :protect-content.

  (https://core.telegram.org/bots/api#forwardmessages)"
  [bot chat-id from-chat-id message-ids & options]
  (let [{:keys [message-thread-id
                disable-notification
                protect-content]} options]
    (h/request bot "forwardMessages" {"chat_id" chat-id
                                      "message_thread_id" message-thread-id
                                      "from_chat_id" from-chat-id
                                      "message_ids" message-ids
                                      "disable_notification" disable-notification
                                      "protect_content" protect-content})))

(defn copy-message
  "Copy a message.

  `options` include: :message-thread-id, :caption, :parse-mode, :caption-entities, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#copymessage)"
  [bot chat-id from-chat-id message-id & options]
  (let [{:keys [message-thread-id
                caption
                parse-mode
                caption-entities
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "copyMessage" {"chat_id" chat-id
                                  "message_thread_id" message-thread-id
                                  "from_chat_id" from-chat-id
                                  "message_id" message-id
                                  "caption" caption
                                  "parse_mode" parse-mode
                                  "caption_entities" caption-entities
                                  "disable_notification" disable-notification
                                  "protect_content" protect-content
                                  "reply_parameters" reply-parameters
                                  "reply_markup" reply-markup})))

(defn copy-messages
  "Copy messages.

  `options` include: :message-thread-id, :disable-notification, :protect-content, and :remove-caption.

  (https://core.telegram.org/bots/api#copymessages)"
  [bot chat-id from-chat-id message-ids & options]
  (let [{:keys [message-thread-id
                disable-notification
                protect-content
                remove-caption]} options]
    (h/request bot "copyMessages" {"chat_id" chat-id
                                   "message_thread_id" message-thread-id
                                   "from_chat_id" from-chat-id
                                   "message_ids" message-ids
                                   "disable_notification" disable-notification
                                   "protect_content" protect-content
                                   "remove_caption" remove-caption})))

(defn send-photo
  "Send a photo.

  `options` include: :message-thread-id, :caption, :parse-mode, :caption-entities, :has-spoiler, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendphoto)"
  [bot chat-id photo & options]
  (let [{:keys [message-thread-id
                caption
                parse-mode
                caption-entities
                has-spoiler
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendPhoto" {"chat_id" chat-id
                                "message_thread_id" message-thread-id
                                "photo" photo
                                "caption" caption
                                "parse_mode" parse-mode
                                "caption_entities" caption-entities
                                "has_spoiler" has-spoiler
                                "disable_notification" disable-notification
                                "protect_content" protect-content
                                "reply_parameters" reply-parameters
                                "reply_markup" reply-markup})))

(defn send-audio
  "Send an audio file.

  `options` include: :message-thread-id, :caption, :parse-mode, :caption-entities, :duration, :performer, :title, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendaudio)"
  [bot chat-id audio & options]
  (let [{:keys [message-thread-id
                caption
                parse-mode
                caption-entities
                duration
                performer
                title
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendAudio" {"chat_id" chat-id
                                "message_thread_id" message-thread-id
                                "audio" audio
                                "caption" caption
                                "parse_mode" parse-mode
                                "caption_entities" caption-entities
                                "duration" duration
                                "performer" performer
                                "title" title
                                "disable_notification" disable-notification
                                "protect_content" protect-content
                                "reply_parameters" reply-parameters
                                "reply_markup" reply-markup})))

(defn send-document
  "Send a document file.

  `options` include: :message-thread-id, :caption, :parse-mode, :caption-entities, :disable-content-type-detection, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#senddocument)"
  [bot chat-id document & options]
  (let [{:keys [message-thread-id
                caption
                parse-mode
                caption-entities
                disable-content-type-detection
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendDocument" {"chat_id" chat-id
                                   "message_thread_id" message-thread-id
                                   "document" document
                                   "caption" caption
                                   "parse_mode" parse-mode
                                   "caption_entities" caption-entities
                                   "disable_content_type_detection" disable-content-type-detection
                                   "disable_notification" disable-notification
                                   "protect_content" protect-content
                                   "reply_parameters" reply-parameters
                                   "reply_markup" reply-markup})))

(defn send-sticker
  "Send a sticker.

  `options` include: :message-thread-id, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendsticker)"
  [bot chat-id sticker & options]
  (let [{:keys [message-thread-id
                emoji
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendSticker" {"chat_id" chat-id
                                  "message_thread_id" message-thread-id
                                  "sticker" sticker
                                  "emoji" emoji
                                  "disable_notification" disable-notification
                                  "protect_content" protect-content
                                  "reply_parameters" reply-parameters
                                  "reply_markup" reply-markup})))

(defn get-sticker-set
  "Fetch a sticker set.

  (https://core.telegram.org/bots/api#getstickerset)"
  [bot name]
  (h/request bot "getStickerSet" {"name" name}))

(defn get-custom-emoji-stickers
  "Fetch custom emoji stickers.

  (https://core.telegram.org/bots/api#getcustomemojistickers)"
  [bot ids]
  (h/request bot "getCustomEmojiStickers" {"custom_emoji_ids" ids}))

(defn upload-sticker-file
  "Upload a sticker file.

  (https://core.telegram.org/bots/api#uploadstickerfile)"
  [bot user-id sticker sticker-format]
  (h/request bot "uploadStickerFile" {"user_id" user-id
                                      "sticker" sticker
                                      "sticker_format" sticker-format}))

(defn create-new-sticker-set
  "Create a new sticker set.

  `options` include: :sticker-type, and :needs-repainting

  (https://core.telegram.org/bots/api#createnewstickerset)"
  [bot user-id name title stickers sticker-format & options]
  (let [{:keys [sticker-type
                needs-repainting]} options]
    (h/request bot "createNewStickerSet" {"user_id" user-id
                                          "name" name
                                          "title" title
                                          "stickers" stickers
                                          "sticker_format" sticker-format
                                          "sticker_type" sticker-type
                                          "needs_repainting" needs-repainting})))

(defn add-sticker-to-set
  "Add a sticker to a set.

  (https://core.telegram.org/bots/api#addstickertoset)"
  [bot user-id name sticker]
  (h/request bot "addStickerToSet" {"user_id" user-id
                                    "name" name
                                    "sticker" sticker}))

(defn set-sticker-position-in-set
  "Set a sticker's position in its set.

  (https://core.telegram.org/bots/api#setstickerpositioninset)"
  [bot sticker position]
  (h/request bot "setStickerPositionInSet" {"sticker" sticker
                                            "position" position}))

(defn delete-sticker-from-set
  "Delete a sticker from its set.

  (https://core.telegram.org/bots/api#deletestickerfromset)"
  [bot sticker]
  (h/request bot "deleteStickerFromSet" {"sticker" sticker}))

(defn set-sticker-set-thumbnail
  "Set thumbnail of a sticker set.

  `options` include: thumbnail.

  (https://core.telegram.org/bots/api#setstickersetthumbnail)"
  [bot name user-id & options]
  (let [{:keys [thumbnail]} options]
    (h/request bot "setStickerSetThumbnail" {"name" name
                                             "user_id" user-id
                                             "thumbnail" thumbnail})))

(defn set-custom-emoji-sticker-set-thumbnail
  "Set thumbnail of custom emoji sticker set.

  `options` include: custom_emoji_id.

  (https://core.telegram.org/bots/api#setcustomemojistickersetthumbnail)"
  [bot name & options]
  (let [{:keys [custom-emoji-id]} options]
    (h/request bot "setCustomEmojiStickerSetThumbnail" {"name" name
                                                        "custom_emoji_id" custom-emoji-id})))

(defn set-sticker-set-title
  "Set title of sticker set.

  (https://core.telegram.org/bots/api#setstickersettitle)"
  [bot name title]
  (h/request bot "setStickerSetTitle" {"name" name
                                       "title" title}))

(defn delete-sticker-set
  "Delete sticker set.

  (https://core.telegram.org/bots/api#deletestickerset)"
  [bot name]
  (h/request bot "deleteStickerSet" {"name" name}))

(defn set-sticker-emoji-list
  "Set sticker's emoji list.

  (https://core.telegram.org/bots/api#setstickeremojilist)"
  [bot sticker emoji-list]
  (h/request bot "setStickerEmojiList" {"sticker" sticker
                                        "emoji_list" emoji-list}))

(defn set-sticker-keywords
  "Set sticker's keywords.

  (https://core.telegram.org/bots/api#setstickerkeywords)"
  [bot sticker & options]
  (let [{:keys [keywords]} options]
    (h/request bot "setStickerKeywords" {"sticker" sticker
                                         "keywords" keywords})))

(defn set-sticker-mask-position
  "Set sticker's mask position.

  (https://core.telegram.org/bots/api#setstickermaskposition)"
  [bot sticker & options]
  (let [{:keys [mask-position]} options]
    (h/request bot "setStickerMaskPosition" {"sticker" sticker
                                             "mask_position" mask-position})))

(defn send-video
  "Send a video.

  `options` include: :message-thread-id, :duration, :caption, :parse-mode, :caption-entities, :has-spoiler, :supports-streaming, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendvideo)"
  [bot chat-id video & options]
  (let [{:keys [message-thread-id
                duration
                caption
                parse-mode
                caption-entities
                has-spoiler
                supports-streaming
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendVideo" {"chat_id" chat-id
                                "message_thread_id" message-thread-id
                                "video" video
                                "duration" duration
                                "caption" caption
                                "parse_mode" parse-mode
                                "caption_entities" caption-entities
                                "has_spoiler" has-spoiler
                                "supports_streaming" supports-streaming
                                "disable_notification" disable-notification
                                "protect_content" protect-content
                                "reply_parameters" reply-parameters
                                "reply_markup" reply-markup})))

(defn send-animation
  "Send an animation.

  `options` include: :message-thread-id, :duration, :width, :height, :thumbnail, :caption, :parse-mode, :caption-entities, :has-spoiler, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendanimation)"
  [bot chat-id animation & options]
  (let [{:keys [message-thread-id
                duration
                width
                height
                thumbnail
                caption
                parse-mode
                caption-entities
                has-spoiler
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendAnimation" {"chat_id" chat-id
                                    "message_thread_id" message-thread-id
                                    "animation" animation
                                    "duration" duration
                                    "width" width
                                    "height" height
                                    "thumbnail" thumbnail
                                    "caption" caption
                                    "parse_mode" parse-mode
                                    "caption_entities" caption-entities
                                    "has_spoiler" has-spoiler
                                    "disable_notification" disable-notification
                                    "protect_content" protect-content
                                    "reply_parameters" reply-parameters
                                    "reply_markup" reply-markup})))

(defn send-voice
  "Send a voice. (.ogg format only)

  `options` include: :message-thread-id, :caption, :parse-mode, :caption-entities, :duration, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendvoice)"
  [bot chat-id voice & options]
  (let [{:keys [message-thread-id
                caption
                parse-mode
                caption-entities
                duration
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendVoice" {"chat_id" chat-id
                                "message_thread_id" message-thread-id
                                "voice" voice
                                "caption" caption
                                "parse_mode" parse-mode
                                "caption_entities" caption-entities
                                "duration" duration
                                "disable_notification" disable-notification
                                "protect_content" protect-content
                                "reply_parameters" reply-parameters
                                "reply_markup" reply-markup})))

(defn send-video-note
  "Send a video note.

  `options` include: :message-thread-id, :duration, :length, :thumbnail, :disable-notification, :reply-parameters, and :reply-markup.
  (XXX: API returns 'Bad Request: wrong video note length' when length is not given / 2017.05.19.)

  (https://core.telegram.org/bots/api#sendvideonote)"
  [bot chat-id video-note & options]
  (let [{:keys [message-thread-id
                duration
                length
                thumbnail
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendVideoNote" {"chat_id" chat-id
                                    "message_thread_id" message-thread-id
                                    "video_note" video-note
                                    "duration" duration
                                    "length" length
                                    "thumbnail" thumbnail
                                    "disable_notification" disable-notification
                                    "protect_content" protect-content
                                    "reply_parameters" reply-parameters
                                    "reply_markup" reply-markup})))

(defn send-media-group
  "Send a media group of photos or videos.

  `options` include: :message-thread-id, :disable-notification, :protect-content, and :reply-parameters.

  (https://core.telegram.org/bots/api#sendmediagroup)"
  [bot chat-id media & options]
  (let [{:keys [message-thread-id
                disable-notification
                protect-content
                reply-parameters]} options]
    (h/request bot "sendMediaGroup" {"chat_id" chat-id
                                     "message_thread_id" message-thread-id
                                     "media" media
                                     "disable_notification" disable-notification
                                     "protect_content" protect-content
                                     "reply_parameters" reply-parameters})))

(defn send-location
  "Send a location.

  `options` include: :message-thread-id, :horizontal-accuracy, :live-period, :heading, :proximity-alert-radius, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendlocation)"
  [bot chat-id latitude longitude & options]
  (let [{:keys [message-thread-id
                horizontal-accuracy
                live-period
                heading
                proximity-alert-radius
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendLocation" {"chat_id" chat-id
                                   "message_thread_id" message-thread-id
                                   "latitude" latitude
                                   "longitude" longitude
                                   "horizontal_accuracy" horizontal-accuracy
                                   "live_period" live-period
                                   "heading" heading
                                   "proximity_alert_radius" proximity-alert-radius
                                   "disable_notification" disable-notification
                                   "protect_content" protect-content
                                   "reply_parameters" reply-parameters
                                   "reply_markup" reply-markup})))

(defn send-venue
  "Send a venue.

  `options` include: :message-thread-id, :foursquare-id, :foursquare-type, :google-place-id, :google-place-type, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendvenue)"
  [bot chat-id latitude longitude title address & options]
  (let [{:keys [message-thread-id
                foursquare-id
                foursquare-type
                google-place-id
                google-place-type
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendVenue" {"chat_id" chat-id
                                "message_thread_id" message-thread-id
                                "latitude" latitude
                                "longitude" longitude
                                "title" title
                                "address" address
                                "foursquare_id" foursquare-id
                                "foursquare_type" foursquare-type
                                "google_place_id" google-place-id
                                "google_place_type" google-place-type
                                "disable_notification" disable-notification
                                "protect_content" protect-content
                                "reply_parameters" reply-parameters
                                "reply_markup" reply-markup})))

(defn send-contact
  "Send a contact.

  `options` include: :message-thread-id, :last-name, :vcard, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendcontact)"
  [bot chat-id phone-number first-name & options]
  (let [{:keys [message-thread-id
                last-name
                vcard
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendContact" {"chat_id" chat-id
                                  "message_thread_id" message-thread-id
                                  "phone_number" phone-number
                                  "first_name" first-name
                                  "last_name" last-name
                                  "vcard" vcard
                                  "disable_notification" disable-notification
                                  "protect_content" protect-content
                                  "reply_parameters" reply-parameters
                                  "reply_markup" reply-markup})))

(defn send-poll
  "Send a poll.

  `options` include: :message-thread-id, :is-anonymous, :type, :allows-multiple-answers, :correct-option-id, :explanation, :explanation-parse-mode, :explanation-entities, :open-period, :close-date, :is-closed, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendpoll)"
  [bot chat-id question poll-options & options]
  (let [{:keys [message-thread-id
                is-anonymous
                type
                allows-multiple-answers
                correct-option-id
                explanation
                explanation-parse-mode
                explanation-entities
                open-period
                close-date
                is-closed
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendPoll" {"chat_id" chat-id
                               "message_thread_id" message-thread-id
                               "question" question
                               "options" poll-options
                               "is_anonymous" is-anonymous
                               "type" type
                               "allows_multiple_answers" allows-multiple-answers
                               "correct_option_id" correct-option-id
                               "explanation" explanation
                               "explanation_parse_mode" explanation-parse-mode
                               "explanation_entities" explanation-entities
                               "open_period" open-period
                               "close_date" close-date
                               "is_closed" is-closed
                               "disable_notification" disable-notification
                               "protect_content" protect-content
                               "reply_parameters" reply-parameters
                               "reply_markup" reply-markup})))

(defn stop-poll
  "Stop a poll.

  `options` include: :reply-markup.

  (https://core.telegram.org/bots/api#stoppoll)"
  [bot chat-id message-id & options]
  (let [{:keys [reply-markup]} options]
    (h/request bot "stopPoll" {"chat_id" chat-id
                               "message_id" message-id
                               "reply_markup" reply-markup})))

(defn send-chat-action
  "Send a chat action.

  `options` include: :message-thread-id.

  `action` can be one of: :typing, :upload_photo, :record_video, :upload_video, :record_voice, :upload_voice, :upload_document, :choose_sticker, :find_location, :record_video_note, or :upload_video_note.

  (https://core.telegram.org/bots/api#sendchataction)"
  [bot chat-id action & options]
  (let [{:keys [message-thread-id]} options]
    (h/request bot "sendChatAction" {"chat_id" chat-id
                                     "message_thread_id" message-thread-id
                                     "action" action})))

(defn set-message-reaction
  "Set reactions on a message.

  `options` include: :reaction, and :is-big.

  `reaction` is an array of reaction types(https://core.telegram.org/bots/api)#reactiontype).

  (https://core.telegram.org/bots/api#setmessagereaction)"
  [bot chat-id message-id & options]
  (let [{:keys [reaction
                is-big]} options]
    (h/request bot "setMessageReaction" {"chat_id" chat-id
                                         "message_id" message-id
                                         "reaction" reaction
                                         "is_big" is-big})))

(defn send-dice
  "Send a dice.

  `emoji` can be one of: 🎲, 🎯, 🏀, ⚽, 🎳, or 🎰. (default: 🎲)

  `options` include: :message-thread-id, :emoji, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#senddice)"
  [bot chat-id & options]
  (let [{:keys [message-thread-id
                emoji
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendDice" {"chat_id" chat-id
                               "message_thread_id" message-thread-id
                               "emoji" emoji
                               "disable_notification" disable-notification
                               "protect_content" protect-content
                               "reply_parameters" reply-parameters
                               "reply_markup" reply-markup})))

(defn get-user-profile-photos
  "Fetch user profile photos.

  `options` include: :offset and :limit.

  (https://core.telegram.org/bots/api#getuserprofilephotos)"
  [bot user-id & options]
  (let [{:keys [offset
                limit]} options]
    (h/request bot "getUserProfilePhotos" {"user_id" user-id
                                           "offset" offset
                                           "limit" limit})))

(defn- get-file-url
  "Generate a file's url from given :file-path."
  [bot file-path]
  (h/url-for-filepath bot file-path))

(defn get-file
  "Fetch a file's info.

  (https://core.telegram.org/bots/api#getfile)"
  [bot file-id]
  (let [result (h/request bot "getFile" {"file_id" file-id})]
    (if (:ok result)
      (assoc-in result [:result :file-url] (get-file-url bot (get-in result [:result :file-path])))
      result)))

(defn ban-chat-member
  "Ban a chat member.

  `options` include: :until-date and :revoke-messages

  (https://core.telegram.org/bots/api#banchatmember)"
  [bot chat-id user-id & options]
  (let [{:keys [until-date
                revoke-messages]} options]
    (h/request bot "banChatMember" {"chat_id" chat-id
                                    "user_id" user-id
                                    "until_date" until-date
                                    "revoke_messages" revoke-messages})))

(defn leave-chat
  "Leave a chat.

  (https://core.telegram.org/bots/api#leavechat)"
  [bot chat-id]
  (h/request bot "leaveChat" {"chat_id" chat-id}))

(defn unban-chat-member
  "Unban a chat member.

  `options` include: :only-if-banned

  (https://core.telegram.org/bots/api#unbanchatmember)"
  [bot chat-id user-id & options]
  (let [{:keys [only-if-banned]} options]
    (h/request bot "unbanChatMember" {"chat_id" chat-id
                                      "user_id" user-id
                                      "only_if_banned" only-if-banned})))

(defn restrict-chat-member
  "Restrict a chat member.

  `options` include: :can-send-messages, :can-send-media-messages, :can-send-polls, :can-send-other-messages, :can-add-web-page-previews, :can-change-info, :can-invite-users, :can-pin-messages, :use-independent-chat-permissions, and :until-date.

  (https://core.telegram.org/bots/api#chatpermissions)
  (https://core.telegram.org/bots/api#restrictchatmember)"
  [bot chat-id user-id & options]
  (let [{:keys [can-send-messages
                can-send-media-messages
                can-send-polls
                can-send-other-messages
                can-add-web-page-previews
                can-change-info
                can-invite-users
                can-pin-messages
                use-independent-chat-permissions
                until-date]
         :or {can-send-messages false
              can-send-media-messages false
              can-send-polls false
              can-send-other-messages false
              can-add-web-page-previews false
              can-change-info false
              can-invite-users false
              can-pin-messages false
              use-independent-chat-permissions false}} options]
    (h/request bot "restrictChatMember" {"chat_id" chat-id
                                         "user_id" user-id
                                         "permissions" {"can_send_messages" can-send-messages
                                                        "can_send_media_messages" can-send-media-messages
                                                        "can_send_polls" can-send-polls
                                                        "can_send_other_messages" can-send-other-messages
                                                        "can_add_web_page_previews" can-add-web-page-previews
                                                        "can_change_info" can-change-info
                                                        "can_invite_users" can-invite-users
                                                        "can_pin_messages" can-pin-messages}
                                         "use_independent_chat_permissions" use-independent-chat-permissions
                                         "until_date" until-date})))

(defn promote-chat-member
  "Promote a chat member.

  `options` include: :is-anonymous, :can-manage-chat, :can-change-info, :can-post-messages, :can-edit-messages, :can-delete-messages, :can-post-stories, :can-edit-stories, :can-delete-stories, :can-manage-video-chats, :can-invite-users, :can-restrict-members, :can-pin-messages, :can-promote-members, and :can-manage-topics.

  (https://core.telegram.org/bots/api#promotechatmember)"
  [bot chat-id user-id & options]
  (let [{:keys [is-anonymous
                can-manage-chat
                can-change-info
                can-post-messages
                can-edit-messages
                can-delete-messages
                can-post-stories
                can-edit-stories
                can-delete-stories
                can-manage-video-chats
                can-invite-users
                can-restrict-members
                can-pin-messages
                can-promote-members
                can-manage-topics]} options]
    (h/request bot "promoteChatMember" {"chat_id" chat-id
                                        "user_id" user-id
                                        "is_anonymous" is-anonymous
                                        "can_manage_chat" can-manage-chat
                                        "can_change_info" can-change-info
                                        "can_post_messages" can-post-messages
                                        "can_edit_messages" can-edit-messages
                                        "can_delete_messages" can-delete-messages
                                        "can_post_stories" can-post-stories
                                        "can_edit_stories" can-edit-stories
                                        "can_delete_stories" can-delete-stories
                                        "can_manage_video_chats" can-manage-video-chats
                                        "can_invite_users" can-invite-users
                                        "can_restrict_members" can-restrict-members
                                        "can_pin_messages" can-pin-messages
                                        "can_promote_members" can-promote-members
                                        "can_manage_topics" can-manage-topics})))

(defn set-chat-administrator-custom-title
  "Set chat administrator's custom title.

  (https://core.telegram.org/bots/api#setchatadministratorcustomtitle)"
  [bot chat-id user-id custom-title]
  (h/request bot "setChatAdministratorCustomTitle" {"chat_id" chat-id
                                                    "user_id" user-id
                                                    "custom_title" custom-title}))

(defn ban-chat-sender-chat
  "Ban a channel chat in a supergroup or a channel.

  (https://core.telegram.org/bots/api#banchatsenderchat)"
  [bot chat-id sender-chat-id]
  (h/request bot "banChatSenderChat" {"chat_id" chat-id
                                      "sender_chat_id" sender-chat-id}))

(defn unban-chat-sender-chat
  "Unban a previously banned channel in a supergroup or a channel.

  (https://core.telegram.org/bots/api#unbanchatsenderchat)"
  [bot chat-id sender-chat-id]
  (h/request bot "unbanChatSenderChat" {"chat_id" chat-id
                                        "sender_chat_id" sender-chat-id}))

(defn set-chat-permissions
  "Set chat permissions.

  `options` include: :can-send-messages, :can-send-media-messages, :can-send-polls, :can-send-other-messages, :can-add-web-page-previews, :can-change-info, :can-invite-users, :can-pin-messages, and :use-independent-chat-permissions.

  (https://core.telegram.org/bots/api#setchatpermissions)"
  [bot chat-id & options]
  (let [{:keys [can-send-messages
                can-send-media-messages
                can-send-polls
                can-send-other-messages
                can-add-web-page-previews
                can-change-info
                can-invite-users
                can-pin-messages
                use-independent-chat-permissions]
         :or {can-send-messages false
              can-send-media-messages false
              can-send-polls false
              can-send-other-messages false
              can-add-web-page-previews false
              can-change-info false
              can-invite-users false
              can-pin-messages false
              use-independent-chat-permissions false}} options]
    (h/request bot "setChatPermissions" {"chat_id" chat-id
                                         "permissions" {"can_send_messages" can-send-messages
                                                        "can_send_media_messages" can-send-media-messages
                                                        "can_send_polls" can-send-polls
                                                        "can_send_other_messages" can-send-other-messages
                                                        "can_add_web_page_previews" can-add-web-page-previews
                                                        "can_change_info" can-change-info
                                                        "can_invite_users" can-invite-users
                                                        "can_pin_messages" can-pin-messages
                                                        "use_independent_chat_permissions" use-independent-chat-permissions}})))

(defn export-chat-invite-link
  "Export a chat invite link.

  (https://core.telegram.org/bots/api#exportchatinvitelink)"
  [bot chat-id]
  (h/request bot "exportChatInviteLink" {"chat_id" chat-id}))

(defn create-chat-invite-link
  "Create a chat invite link.

  (https://core.telegram.org/bots/api#createchatinvitelink)"
  [bot chat-id & options]
  (let [{:keys [name
                expire-date
                member-limit
                creates-join-request]} options]
    (h/request bot "createChatInviteLink" {"chat_id" chat-id
                                           "name" name
                                           "expire_date" expire-date
                                           "member_limit" member-limit
                                           "creates_join_request" creates-join-request})))

(defn edit-chat-invite-link
  "Edit a chat invite link.

  (https://core.telegram.org/bots/api#editchatinvitelink)"
  [bot chat-id invite-link & options]
  (let [{:keys [name
                expire-date
                member-limit
                creates-join-request]} options]
    (h/request bot "editChatInviteLink" {"chat_id" chat-id
                                         "name" name
                                         "invite_link" invite-link
                                         "expire_date" expire-date
                                         "member_limit" member-limit
                                         "creates_join_request" creates-join-request})))

(defn revoke-chat-invite-link
  "Revoke a chat invite link.

  (https://core.telegram.org/bots/api#revokechatinvitelink)"
  [bot chat-id invite-link]
  (h/request bot "revokeChatInviteLink" {"chat_id" chat-id
                                         "invite_link" invite-link}))

(defn approve-chat-join-request
  "Approve chat join request.

  (https://core.telegram.org/bots/api#approvechatjoinrequest)"
  [bot chat-id user-id]
  (h/request bot "approveChatJoinRequest" {"chat_id" chat-id
                                           "user_id" user-id}))

(defn decline-chat-join-request
  "Decline chat join request.

  (https://core.telegram.org/bots/api#declinechatjoinrequest)"
  [bot chat-id user-id]
  (h/request bot "declineChatJoinRequest" {"chat_id" chat-id
                                           "user_id" user-id}))

(defn set-chat-photo
  "Set a chat photo.

  (https://core.telegram.org/bots/api#setchatphoto)"
  [bot chat-id photo]
  (h/request bot "setChatPhoto" {"chat_id" chat-id
                                 "photo" photo}))

(defn delete-chat-photo
  "Delete a chat photo.

  (https://core.telegram.org/bots/api#deletechatphoto)"
  [bot chat-id]
  (h/request bot "deleteChatPhoto" {"chat_id" chat-id}))

(defn set-chat-title
  "Set a chat title.

  (https://core.telegram.org/bots/api#setchattitle)"
  [bot chat-id title]
  (h/request bot "setChatTitle" {"chat_id" chat-id
                                 "title" title}))

(defn set-chat-description
  "Set a chat description.

  (https://core.telegram.org/bots/api#setchatdescription)"
  [bot chat-id description]
  (h/request bot "setChatDescription" {"chat_id" chat-id
                                       "description" description}))

(defn pin-chat-message
  "Pin a chat message.

  `options` include: :disable-notification.

  (https://core.telegram.org/bots/api#pinchatmessage)"
  [bot chat-id message-id & options]
  (let [{:keys [disable-notification]} options]
    (h/request bot "pinChatMessage" {"chat_id" chat-id
                                     "message_id" message-id
                                     "disable_notification" disable-notification})))

(defn unpin-chat-message
  "Unpin a chat message.

  (https://core.telegram.org/bots/api#unpinchatmessage)"
  [bot chat-id & options]
  (let [{:keys [message-id]} options]
    (h/request bot "unpinChatMessage" {"chat_id" chat-id
                                       "message_id" message-id})))

(defn unpin-all-chat-messages
  "Unpin all chat messages.

  (https://core.telegram.org/bots/api#unpinallchatmessages)"
  [bot chat-id]
  (h/request bot "unpinAllChatMessages" {"chat_id" chat-id}))

(defn get-chat
  "Fetch a chat.

  (https://core.telegram.org/bots/api#getchat)"
  [bot chat-id]
  (h/request bot "getChat" {"chat_id" chat-id}))

(defn get-chat-administrators
  "Fetch chat administrators.

  (https://core.telegram.org/bots/api#getchatadministrators)"
  [bot chat-id]
  (h/request bot "getChatAdministrators" {"chat_id" chat-id}))

(defn get-chat-member-count
  "Fetch the count of chat members.

  (https://core.telegram.org/bots/api#getchatmembercount)"
  [bot chat-id]
  (h/request bot "getChatMemberCount" {"chat_id" chat-id}))

(defn get-chat-member
  "Fetch a chat member.

  (https://core.telegram.org/bots/api#getchatmember)"
  [bot chat-id user-id]
  (h/request bot "getChatMember" {"chat_id" chat-id
                                  "user_id" user-id}))

(defn set-chat-sticker-set
  "Set a chat sticker set.

  (https://core.telegram.org/bots/api#setchatstickerset)"
  [bot chat-id sticker-set-name]
  (h/request bot "setChatStickerSet" {"chat_id" chat-id
                                      "sticker_set_name" sticker-set-name}))

(defn delete-chat-sticker-set
  "Delete a chat sticker set.

  (https://core.telegram.org/bots/api#deletechatstickerset)"
  [bot chat-id]
  (h/request bot "deleteChatStickerSet" {"chat_id" chat-id}))

(defn answer-callback-query
  "Answer a callback query.

  `options` include: :text, :show-alert, :url, and :cache-time.

  (https://core.telegram.org/bots/api#answercallbackquery)"
  [bot callback-query-id & options]
  (let [{:keys [text
                show-alert
                url
                cache-time]} options]
    (h/request bot "answerCallbackQuery" {"callback_query_id" callback-query-id
                                          "text" text
                                          "show_alert" show-alert
                                          "url" url
                                          "cache_time" cache-time})))

(defn get-user-chat-boosts
  "Get chat boosts of a user.

  (https://core.telegram.org/bots/api#getuserchatboosts)"
  [bot chat-id user-id]
  (h/request bot "getUserChatBoosts" {"chat_id" chat-id
                                      "user_id" user-id}))

(defn get-my-commands
  "Get this bot's commands.

  (https://core.telegram.org/bots/api#getmycommands)"
  [bot & options]
  (let [{:keys [scope
                language-code]} options]
    (h/request bot "getMyCommands" {"scope" scope
                                    "language_code" language-code})))

(defn set-my-name
  "Set this bot's name.

  (https://core.telegram.org/bots/api#setmyname)"
  [bot name & options]
  (let [{:keys [language-code]} options]
    (h/request bot "setMyName" {"name" name
                                "language_code" language-code})))

(defn get-my-name
  "Get this bot's name.

  (https://core.telegram.org/bots/api#getmyname)"
  [bot & options]
  (let [{:keys [language-code]} options]
    (h/request bot "getMyName" {"language_code" language-code})))

(defn set-my-description
  "Set this bot's description.

  (https://core.telegram.org/bots/api#setmydescription)"
  [bot & options]
  (let [{:keys [description
                language-code]} options]
    (h/request bot "setMyDescription" {"description" description
                                       "language_code" language-code})))

(defn get-my-description
  "Get this bot's description.

  (https://core.telegram.org/bots/api#getmydescription)"
  [bot & options]
  (let [{:keys [language-code]} options]
    (h/request bot "getMyDescription" {"language_code" language-code})))

(defn set-my-short-description
  "Set this bot's short description.

  (https://core.telegram.org/bots/api#setmyshortdescription)"
  [bot & options]
  (let [{:keys [short-description
                language-code]} options]
    (h/request bot "setMyShortDescription" {"short_description" short-description
                                            "language_code" language-code})))

(defn get-my-short-description
  "Get this bot's short description.

  (https://core.telegram.org/bots/api#getmyshortdescription)"
  [bot & options]
  (let [{:keys [language-code]} options]
    (h/request bot "getMyShortDescription" {"language_code" language-code})))

(defn set-my-commands
  "Set this bot's commands.

  (https://core.telegram.org/bots/api#setmycommands)"
  [bot commands & options]
  (let [{:keys [scope
                language-code]} options]
    (h/request bot "setMyCommands" {"commands" commands
                                    "scope" scope
                                    "language_code" language-code})))

(defn delete-my-commands
  "Delete this bot's commands.

  (https://core.telegram.org/bots/api#deletemycommands)"
  [bot & options]
  (let [{:keys [scope
                language-code]} options]
    (h/request bot "deleteMyCommands" {"scope" scope
                                       "language_code" language-code})))

(defn set-chat-menu-button
  "Set the bot's menu button.

  `options` include: :chat-id, and :menu-button.

  (https://core.telegram.org/bots/api#setchatmenubutton)"
  [bot & options]
  (let [{:keys [chat-id
                menu-button]} options]
    (h/request bot "setChatMenuButton" {"chat_id" chat-id
                                        "menu_button" menu-button})))

(defn get-chat-menu-button
  "Get the bot's menu button.

  `options` include: :chat-id.

  (https://core.telegram.org/bots/api#getchatmenubutton)"
  [bot & options]
  (let [{:keys [chat-id]} options]
    (h/request bot "getChatMenuButton" {"chat_id" chat-id})))

(defn set-my-default-administrator-rights
  "Set my default administrator rights.

  `options` include: :rights, and :for-channels.

  (https://core.telegram.org/bots/api#setmydefaultadministratorrights)"
  [bot & options]
  (let [{:keys [rights
                for-channels]} options]
    (h/request bot "setMyDefaultAdministratorRights" {"rights" rights
                                                      "for_channels" for-channels})))

(defn get-my-default-administrator-rights
  "Get my default administrator rights.

  `options` include: :for-channels.

  (https://core.telegram.org/bots/api#getmydefaultadministratorrights)"
  [bot & options]
  (let [{:keys [for-channels]} options]
    (h/request bot "getMyDefaultAdministratorRights" {"for_channels" for-channels})))

(defn edit-message-text
  "Edit a message's text.

  required `options`: :chat-id + :message-id (when :inline-message-id is not given)
                      or :inline-message-id (when :chat-id & :message-id are not given)

  other `options` include: :parse-mode, :entities, :link-preview-options, and :reply-markup.

  (https://core.telegram.org/bots/api#editmessagetext)"
  [bot text & options]
  (let [{:keys [chat-id
                message-id
                inline-message-id
                parse-mode
                entities
                link-preview-options
                reply-markup]} options]
    (h/request bot "editMessageText" {"text" text
                                      "chat_id" chat-id
                                      "message_id" message-id
                                      "inline_message_id" inline-message-id
                                      "parse_mode" parse-mode
                                      "entities" entities
                                      "link_preview_options" link-preview-options
                                      "reply_markup" reply-markup})))

(defn edit-message-caption
  "Edit a message's caption.

  required `options`: :chat-id + :message-id (when :inline-message-id is not given)
                      or :inline-message-id (when :chat-id & :message-id are not given)

  other `options` include: :parse-mode, :caption-entities, and :reply-markup.

  (https://core.telegram.org/bots/api#editmessagecaption)"
  [bot caption & options]
  (let [{:keys [chat-id
                message-id
                inline-message-id
                parse-mode
                caption-entities
                reply-markup]} options]
    (h/request bot "editMessageCaption" {"caption" caption
                                         "chat_id" chat-id
                                         "message_id" message-id
                                         "inline_message_id" inline-message-id
                                         "parse_mode" parse-mode
                                         "caption_entities" caption-entities
                                         "reply_markup" reply-markup})))

(defn edit-message-media
  "Edit a message's media.

  required `options`: :chat-id + :message-id (when :inline-message-id is not given)
                      or :inline-message-id (when :chat-id & :message-id are not given)

  other `options` include: :reply-markup.

  (https://core.telegram.org/bots/api#editmessagemedia)"
  [bot media & options]
  (let [{:keys [chat-id
                message-id
                inline-message-id
                reply-markup]} options]
    (h/request bot "editMessageMedia" {"media" media
                                       "chat_id" chat-id
                                       "message_id" message-id
                                       "inline_message_id" inline-message-id
                                       "reply_markup" reply-markup})))

(defn edit-message-reply-markup
  "Edit a message's reply markup.

  required `options`: :chat-id + :message-id (when :inline-message-id is not given)
                      or :inline-message-id (when :chat-id & :message-id are not given)

  other `options` include: :reply-markup.

  (https://core.telegram.org/bots/api#editmessagereplymarkup)"
  [bot & options]
  (let [{:keys [chat-id
                message-id
                inline-message-id
                reply-markup]} options]
    (h/request bot "editMessageReplyMarkup" {"chat_id" chat-id
                                             "message_id" message-id
                                             "inline_message_id" inline-message-id
                                             "reply_markup" reply-markup})))

(defn edit-message-live-location
  "Edit a message's live location.

  required `options`: :chat-id + :message-id (when :inline-message-id is not given)
                      or :inline-message-id (when :chat-id & :message-id are not given)

  other `options` include: :horizontal-accuracy, :heading, :proximity-alert-radius, and :reply-markup.

  (https://core.telegram.org/bots/api#editmessagelivelocation)"
  [bot latitude longitude & options]
  (let [{:keys [chat-id
                message-id
                inline-message-id
                horizontal-accuracy
                heading
                proximity-alert-radius
                reply-markup]} options]
    (h/request bot "editMessageLiveLocation" {"chat_id" chat-id
                                              "message_id" message-id
                                              "inline_message_id" inline-message-id
                                              "latitude" latitude
                                              "longitude" longitude
                                              "horizontal_accuracy" horizontal-accuracy
                                              "heading" heading
                                              "proximity_alert_radius" proximity-alert-radius
                                              "reply_markup" reply-markup})))

(defn stop-message-live-location
  "Stop a message's live location.

  required `options`: :chat-id + :message-id (when :inline-message-id is not given)
                      or :inline-message-id (when :chat-id & :message-id are not given)

  other `options` include: :reply-markup.

  (https://core.telegram.org/bots/api#stopmessagelivelocation)"
  [bot & options]
  (let [{:keys [chat-id
                message-id
                inline-message-id
                reply-markup]} options]
    (h/request bot "stopMessageLiveLocation" {"chat_id" chat-id
                                              "message_id" message-id
                                              "inline_message_id" inline-message-id
                                              "reply_markup" reply-markup})))

(defn delete-message
  "Delete a message.

  (https://core.telegram.org/bots/api#deletemessage)"
  [bot chat-id message-id]
  (h/request bot "deleteMessage" {"chat_id" chat-id
                                  "message_id" message-id}))

(defn delete-messages
  "Delete messages.

  (https://core.telegram.org/bots/api#deletemessages)"
  [bot chat-id message-ids]
  (h/request bot "deleteMessages" {"chat_id" chat-id
                                   "message_ids" message-ids}))

(defn answer-inline-query
  "Answer an inline query.

  `options` include: :cache-time, :is-personal, :next-offset, :switch-pm-text, and :switch-pm-parameter.

  (https://core.telegram.org/bots/api#answerinlinequery)"
  [bot inline-query-id results & options]
  (let [{:keys [cache-time
                is-personal
                next-offset
                switch-pm-text
                switch-pm-parameter]} options]
    (h/request bot "answerInlineQuery" {"inline_query_id" inline-query-id
                                        "results" results
                                        "cache_time" cache-time
                                        "is_personal" is-personal
                                        "next_offset" next-offset
                                        "switch_pm_text" switch-pm-text
                                        "switch_pm_parameter" switch-pm-parameter})))

(defn send-invoice
  "Send an invoice.

  `options` include: :message-thread-id, :max-tip-amount, :suggested-tip-amounts, :start-parameter, :provider-data, :photo-url, :photo-size, :photo-width, :photo-height, :need-name, :need-phone-number, :need-email, :need-shipping-address, :send-phone-number-to-provider, :send-email-to-provider, :is-flexible, :disable-notification, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendinvoice)"
  [bot chat-id title description payload provider-token currency prices & options]
  (let [{:keys [message-thread-id
                max-tip-amount
                suggested-tip-amounts
                start-parameter
                provider-data
                photo-url
                photo-size
                photo-width
                photo-height
                need-name
                need-phone-number
                need-email
                need-shipping-address
                send-phone-number-to-provider
                send-email-to-provider
                is-flexible
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendInvoice" {"chat_id" chat-id
                                  "message_thread_id" message-thread-id
                                  "title" title
                                  "description" description
                                  "payload" payload
                                  "provider_token" provider-token
                                  "currency" currency
                                  "prices" prices
                                  "max_tip_amount" max-tip-amount
                                  "suggested_tip_amounts" suggested-tip-amounts
                                  "start_parameter" start-parameter
                                  "provider_data" provider-data
                                  "photo_url" photo-url
                                  "photo_size" photo-size
                                  "photo_width" photo-width
                                  "photo_height" photo-height
                                  "need_name" need-name
                                  "need_phone_number" need-phone-number
                                  "need_email" need-email
                                  "need_shipping_address" need-shipping-address
                                  "send_phone_number_to_provider" send-phone-number-to-provider
                                  "send_email_to_provider" send-email-to-provider
                                  "is_flexible" is-flexible
                                  "disable_notification" disable-notification
                                  "protect_content" protect-content
                                  "reply_parameters" reply-parameters
                                  "reply_markup" reply-markup})))

(defn create-invoice-link
  "Create a link for an invoice.

  `options` include: :max-tip-amount, :suggested-tip-amounts, :provider-data, :photo-url, :photo-size, :photo-width, :photo-height, :need-name, :need-phone-number, :need-email, :need-shipping-address, :send-phone-number-to-provider, :send-email-to-provider, and :is-flexible.

  https://core.telegram.org/bots/api#createinvoicelink"
  [bot chat-id title description payload provider-token currency prices & options]
  (let [{:keys [max-tip-amount
                suggested-tip-amounts
                provider-data
                photo-url
                photo-size
                photo-width
                photo-height
                need-name
                need-phone-number
                need-email
                need-shipping-address
                send-phone-number-to-provider
                send-email-to-provider
                is-flexible]} options]
    (h/request bot "createInvoiceLink" {"chat_id" chat-id
                                        "title" title
                                        "description" description
                                        "payload" payload
                                        "provider_token" provider-token
                                        "currency" currency
                                        "prices" prices
                                        "max_tip_amount" max-tip-amount
                                        "suggested_tip_amounts" suggested-tip-amounts
                                        "provider_data" provider-data
                                        "photo_url" photo-url
                                        "photo_size" photo-size
                                        "photo_width" photo-width
                                        "photo_height" photo-height
                                        "need_name" need-name
                                        "need_phone_number" need-phone-number
                                        "need_email" need-email
                                        "need_shipping_address" need-shipping-address
                                        "send_phone_number_to_provider" send-phone-number-to-provider
                                        "send_email_to_provider" send-email-to-provider
                                        "is_flexible" is-flexible})))

(defn answer-shipping-query
  "Answer a shipping query.

  If `ok` is true, :shipping-options should be included in `options`. Otherwise, :error-message should be included.

  (https://core.telegram.org/bots/api#answershippingquery)"
  [bot shipping-query-id ok & options]
  (let [{:keys [shipping-options
                error-message]} options]
    (h/request bot "answerShippingQuery" {"shipping_query_id" shipping-query-id
                                          "ok" ok
                                          "shipping_options" shipping-options
                                          "error_message" error-message})))

(defn answer-pre-checkout-query
  "Answer a pre-checkout query.

  If `ok` is false, :error-message should be included in `options`.

  (https://core.telegram.org/bots/api#answerprecheckoutquery)"
  [bot pre-checkout-query-id ok & options]
  (let [{:keys [error-message]} options]
    (h/request bot "answerPreCheckoutQuery" {"pre_checkout_query_id" pre-checkout-query-id
                                             "ok" ok
                                             "error_message" error-message})))

(defn answer-web-app-query
  "Answer a web app query.

  (https://core.telegram.org/bots/api#answerwebappquery)"
  [bot web-app-query-id result]
  (h/request bot "answerWebAppQuery" {"web_app_query_id" web-app-query-id
                                      "result" result}))

(defn send-game
  "Send a game.

  `options` include: :message-thread-id, :disable-notification, :protect-content, :reply-parameters, and :reply-markup.

  (https://core.telegram.org/bots/api#sendgame)"
  [bot chat-id game-short-name & options]
  (let [{:keys [message-thread-id
                disable-notification
                protect-content
                reply-parameters
                reply-markup]} options]
    (h/request bot "sendGame" {"chat_id" chat-id
                               "message_thread_id" message-thread-id
                               "game_short_name" game-short-name
                               "disable_notification" disable-notification
                               "protect_content" protect-content
                               "reply_parameters" reply-parameters
                               "reply_markup" reply-markup})))

(defn set-game-score
  "Set score for a game.

  required `options`: :chat-id + :message-id (when :inline-message-id is not given)
                      or :inline-message-id (when :chat-id & :message-id are not given)

  other `options` include: :force, and :disable-edit-message.

  (https://core.telegram.org/bots/api#setgamescore)"
  [bot user-id score & options]
  (let [{:keys [chat-id
                message-id
                inline-message-id
                force
                disable-edit-message]} options]
    (h/request bot "setGameScore" {"user_id" user-id
                                   "score" score
                                   "chat_id" chat-id
                                   "message_id" message-id
                                   "inline_message_id" inline-message-id
                                   "force" force
                                   "disable_edit_message" disable-edit-message})))

(defn get-game-highscores
  "Fetch a game's highscores.

  required `options`: :chat-id + :message-id (when :inline-message-id is not given)
                      or :inline-message-id (when :chat-id & :message-id are not given)

  (https://core.telegram.org/bots/api#getgamehighscores)"
  [bot user-id & options]
  (let [{:keys [chat-id
                message-id
                inline-message-id]} options]
    (h/request bot "getGameHighScores" {"user_id" user-id
                                        "chat_id" chat-id
                                        "message_id" message-id
                                        "inline_message_id" inline-message-id})))

(defn create-forum-topic
  "Create a topic in a forum supergroup chat.

  (https://core.telegram.org/bots/api#createforumtopic)"
  [bot chat-id name & options]
  (let [{:keys [icon-color
                icon-custom-emoji-id]} options]
    (h/request bot "createForumTopic" {"chat_id" chat-id
                                       "name" name
                                       "icon_color" icon-color
                                       "icon_custom_emoji_id" icon-custom-emoji-id})))

(defn edit-forum-topic
  "Edit name and icon of a topic in a forum supergroup chat.

  `options` include: :name, and :icon-custom-emoji-id.

  (https://core.telegram.org/bots/api#editforumtopic)"
  [bot chat-id message-thread-id & options]
  (let [{:keys [name
                icon-custom-emoji-id]} options]
    (h/request bot "editForumTopic" {"chat_id" chat-id
                                     "message_thread_id" message-thread-id
                                     "name" name
                                     "icon_custom_emoji_id" icon-custom-emoji-id})))

(defn close-forum-topic
  "Close an open topic in a forum supergroup chat.

  (https://core.telegram.org/bots/api#closeforumtopic)"
  [bot chat-id message-thread-id]
  (h/request bot "closeForumTopic" {"chat_id" chat-id
                                    "message_thread_id" message-thread-id}))

(defn reopen-forum-topic
  "Reopen a closed topic in a forum supergroup chat.

  (https://core.telegram.org/bots/api#reopenforumtopic)"
  [bot chat-id message-thread-id]
  (h/request bot "reopenForumTopic" {"chat_id" chat-id
                                     "message_thread_id" message-thread-id}))

(defn delete-forum-topic
  "Delete a forum topic along with all its messages in a forum supergroup chat.

  (https://core.telegram.org/bots/api#deleteforumtopic)"
  [bot chat-id message-thread-id]
  (h/request bot "deleteForumTopic" {"chat_id" chat-id
                                     "message_thread_id" message-thread-id}))

(defn unpin-all-forum-topic-messages
  "Clear the list of pinned messages in a forum topic.

  (https://core.telegram.org/bots/api#unpinallforumtopicmessages)"
  [bot chat-id message-thread-id]
  (h/request bot "unpinAllForumTopicMessages" {"chat_id" chat-id
                                               "message_thread_id" message-thread-id}))

(defn edit-general-forum-topic
  "Edit the name of the 'General' topic in a forum supergroup chat.

  (https://core.telegram.org/bots/api#editgeneralforumtopic)"
  [bot chat-id name]
  (h/request bot "editGeneralForumTopic" {"chat_id" chat-id
                                          "name" name}))

(defn close-general-forum-topic
  "Close an open 'General' topic in a forum supergroup chat.

  (https://core.telegram.org/bots/api#closegeneralforumtopic)"
  [bot chat-id]
  (h/request bot "closeGeneralForumTopic" {"chat_id" chat-id}))

(defn reopen-general-forum-topic
  "Reopen a closed 'General' topic in a forum supergroup chat.

  (https://core.telegram.org/bots/api#reopengeneralforumtopic)"
  [bot chat-id]
  (h/request bot "reopenGeneralForumTopic" {"chat_id" chat-id}))

(defn hide-general-forum-topic
  "Hide the 'General' topic in a forum supergroup chat.

  (https://core.telegram.org/bots/api#hidegeneralforumtopic)"
  [bot chat-id]
  (h/request bot "hideGeneralForumTopic" {"chat_id" chat-id}))

(defn unhide-general-forum-topic
  "Unhide the 'General' topic in a forum supergroup chat.

  (https://core.telegram.org/bots/api#unhidegeneralforumtopic)"
  [bot chat-id]
  (h/request bot "unhideGeneralForumTopic" {"chat_id" chat-id}))

(defn unpin-all-general-forum-topic-messages
  "Unpin all pinned messages in a general forum topic.

  (https://core.telegram.org/bots/api#unpinallgeneralforumtopicmessages)"
  [bot chat-id]
  (h/request bot "unpinAllGeneralForumTopicMessages" {"chat_id" chat-id}))

(defn get-forum-topic-icon-stickers
  "Get custom emoji stickers.

  (https://core.telegram.org/bots/api#getforumtopiciconstickers)"
  [bot]
  (h/request bot "getForumTopicIconStickers" {}))

