;; Telegram Bot Library for Clojure
;;
;; meinside/clogram.clj
;;
;; (https://core.telegram.org/bots/api)
;;
;; created on 2019.12.05.

(ns meinside.clogram
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]]
            [meinside.clogram.helper :as h])) ;; helper functions

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; constants

(def default-interval-seconds 1)
(def default-timeout-seconds 10)
(def default-limit-count 100)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Bot

(defrecord Bot
  [token
   interval-seconds
   limit-count
   timeout-seconds
   polling?
   polling-wait-ch
   verbose?])

;; create a new bot with given params
(defn new-bot
  "Create a new bot with given token and options"
  [token & opts]
  (let [{:keys [interval-seconds
                limit-count
                timeout-seconds
                verbose?]
         :or {interval-seconds default-interval-seconds
              limit-count default-limit-count
              timeout-seconds default-timeout-seconds
              verbose? false}} opts]
    (do
      (h/log "creating a new bot with options: " opts)
      (map->Bot {:token token
                 :interval-seconds interval-seconds
                 :limit-count limit-count
                 :timeout-seconds timeout-seconds
                 :polling? (atom false)
                 :polling-wait-ch (atom nil)
                 :verbose? verbose?}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; bot API methods
;;
;; (https://core.telegram.org/bots/api#available-methods)

(defn delete-webhook
  "Delete webhook for polling messages.

  (https://core.telegram.org/bots/api#deletewebhook)"
  [bot]
  (h/request bot "deleteWebhook" {}))

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

(defn poll-updates
  "Poll updates for this bot with given interval and send them through the handler function.

  It will block until get stopped with `stop-polling` function.

  `options` include: :offset, :limit, :timeout, and :allowed-updates.

  Start polling with:

  ;; will be blocked until stopped
  (poll-updates bot 1 (fn [bot update] ...))

  and stop polling with:

  (stop-polling bot)"
  [bot interval-seconds fn-update-handler & options]
  (let [polling? (:polling? bot)]
    (if @polling?
      ;; already polling, do nothing
      (h/log "already polling")

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
                     (let [response (get-updates bot
                                                 :offset @update-offset
                                                 :limit limit
                                                 :timeout timeout
                                                 :allowed-updates allowed-updates)]
                       (if (:ok response)
                         (if (not-empty (:result response))
                           (do
                             ;; new update-offset = latest update-id + 1
                             (reset! update-offset (inc (last (sort (map :update_id (:result response))))))

                             ;; callback updates
                             (doseq [update (:result response)]
                               (go (fn-update-handler bot update))))
                           (h/log "no updates..."))
                         (h/log "failed to poll updates: " (:reason-phrase response)))

                       ;; interval
                       (<! (a/timeout (* 1000 interval-seconds)))))

                   ;; out of while-loop
                   (h/log "stopped polling."))]

        ;; reset wait channel,
        (reset! polling-wait-ch wait)

        ;; and wait for it
        (<!! wait)))))

(defn stop-polling
  "Stop polling if the bot was polling."
  [bot]
  (let [polling? (:polling? bot)
        wait (:polling-wait-ch bot)]
    (if (and @polling? @wait)
      (do
        (h/log "stopping polling...")

        ;; make it false
        (reset! polling? false)

        ;; close channel and make it nil
        (a/close! @wait)
        (reset! wait nil))

      (h/log "not polling (anymore)"))))

(defn send-message
  "Send a message.

  `options` include: :parse-mode, :disable-web-page-preview, :disable-notification, :reply-to-message-id, and :reply-markup.

  (https://core.telegram.org/bots/api#sendmessage)"
  [bot chat-id text & options]
  (let [{:keys [parse-mode
                disable-web-page-preview
                disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendMessage" {"chat_id" chat-id
                                  "text" text
                                  "parse_mode" parse-mode
                                  "disable_web_page_preview" disable-web-page-preview
                                  "disable_notification" disable-notification
                                  "reply_to_message_id" reply-to-message-id
                                  "reply_markup" reply-markup})))

(defn forward-message
  "Forward a message.

  `options` include: :disable-notification.

  (https://core.telegram.org/bots/api#forwardmessage)"
  [bot chat-id from-chat-id message-id & options]
  (let [{:keys [disable-notification]} options]
    (h/request bot "forwardMessage" {"chat_id" chat-id
                                     "from_chat_id" from-chat-id
                                     "message_id" message-id
                                     "disable_notification" disable-notification})))

(defn send-photo
  "Send a photo.

  `options` include: :caption, :parse-mode, :disable-notification, :reply-to-message-id, and :reply-markup.

  (https://core.telegram.org/bots/api#sendphoto)"
  [bot chat-id photo & options]
  (let [{:keys [caption
                parse-mode
                disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendPhoto" {"chat_id" chat-id
                                "photo" photo
                                "caption" caption
                                "parse_mode" parse-mode
                                "disable_notification" disable-notification
                                "reply_to_message_id" reply-to-message-id
                                "reply_markup" reply-markup})))

(defn send-audio
  "Send an audio file.

  `options` include: :caption, :parse-mode, :duration, :performer, :title, :disable-notification, :reply-to-message-id, and :reply-markup.

  (https://core.telegram.org/bots/api#sendaudio)"
  [bot chat-id audio & options]
  (let [{:keys [caption
                parse-mode
                duration
                performer
                title
                disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendAudio" {"chat_id" chat-id
                                "audio" audio
                                "caption" caption
                                "parse_mode" parse-mode
                                "duration" duration
                                "performer" performer
                                "title" title
                                "disable_notification" disable-notification
                                "reply_to_message_id" reply-to-message-id
                                "reply_markup" reply-markup})))

(defn send-document
  "Send a document file.

  `options` include: :caption, :parse-mode, :disable-notification, :reply-to-message-id, and :reply-markup.

  (https://core.telegram.org/bots/api#senddocument)"
  [bot chat-id document & options]
  (let [{:keys [caption
                parse-mode
                disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendDocument" {"chat_id" chat-id
                                   "document" document
                                   "caption" caption
                                   "parse_mode" parse-mode
                                   "disable_notification" disable-notification
                                   "reply_to_message_id" reply-to-message-id
                                   "reply_markup" reply-markup})))

(defn send-sticker
  "Send a sticker.

  `options` include: disable_notification, reply_to_message_id, and reply_markup.

  (https://core.telegram.org/bots/api#sendsticker)"
  [bot chat-id sticker & options]
  (let [{:keys [disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendSticker" {"chat_id" chat-id
                                  "sticker" sticker
                                  "disable_notification" disable-notification
                                  "reply_to_message_id" reply-to-message-id
                                  "reply_markup" reply-markup})))

(defn get-sticker-set
  "Fetch a sticker set.

  (https://core.telegram.org/bots/api#getstickerset)"
  [bot name]
  (h/request bot "getStickerSet" {"name" name}))

(defn upload-sticker-file
  "Upload a sticker file.

  (https://core.telegram.org/bots/api#uploadstickerfile)"
  [bot user-id sticker]
  (h/request bot "uploadStickerFile" {"user_id" user-id
                                      "png_sticker" sticker}))

(defn create-new-sticker-set
  "Create a new sticker set.

  `options` include: :contains-masks and :mask-position

  (https://core.telegram.org/bots/api#createnewstickerset)"
  [bot user-id name title sticker emojis & options]
  (let [{:keys [contains-masks
                mask-position]} options]
    (h/request bot "createNewStickerSet" {"user_id" user-id
                                          "name" name
                                          "title" title
                                          "png_sticker" sticker
                                          "emojis" emojis
                                          "contains_masks" contains-masks
                                          "mask_position" mask-position})))

(defn add-sticker-to-set
  "Add a sticker to a set.

  `options` include: :mask-position

  (https://core.telegram.org/bots/api#addstickertoset)"
  [bot user-id name sticker emojis & options]
  (let [{:keys [mask-position]} options]
    (h/request bot "addStickerToSet" {"user_id" user-id
                                      "name" name
                                      "png_sticker" sticker
                                      "emojis" emojis
                                      "mask_position" mask-position})))

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

(defn send-video
  "Send a video.

  `options` include: :duration, :caption, :parse-mode, :supports-streaming, :disable-notification, :reply-to-message-id, and :reply-markup.

  (https://core.telegram.org/bots/api#sendvideo)"
  [bot chat-id video & options]
  (let [{:keys [duration
                caption
                parse-mode
                supports-streaming
                disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendVideo" {"chat_id" chat-id
                                "video" video
                                "duration" duration
                                "caption" caption
                                "parse_mode" parse-mode
                                "supports_streaming" supports-streaming
                                "disable_notification" disable-notification
                                "reply_to_message_id" reply-to-message-id
                                "reply_markup" reply-markup})))

(defn send-animation
  "Send an animation.

  `options` include: :duration, :width, :height, :thumb, :caption, :parse-mode, :disable-notification, :reply-to-message-id, and :reply-markup.

  (https://core.telegram.org/bots/api#sendanimation)"
  [bot chat-id animation & options]
  (let [{:keys [duration
                width
                height
                thumb
                caption
                parse-mode
                disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendAnimation" {"chat_id" chat-id
                                    "animation" animation
                                    "duration" duration
                                    "width" width
                                    "height" height
                                    "thumb" thumb
                                    "caption" caption
                                    "parse_mode" parse-mode
                                    "disable_notification" disable-notification
                                    "reply_to_message_id" reply-to-message-id
                                    "reply_markup" reply-markup})))

(defn send-voice
  "Send a voice. (.ogg format only)

  `options` include: :caption, :parse-mode, :duration, :disable-notification, :reply-to-message-id, and :reply-markup.

  (https://core.telegram.org/bots/api#sendvoice)"
  [bot chat-id voice & options]
  (let [{:keys [caption
                parse-mode
                duration
                disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendVoice" {"chat_id" chat-id
                                "voice" voice
                                "caption" caption
                                "parse_mode" parse-mode
                                "duration" duration
                                "disable_notification" disable-notification
                                "reply_to_message_id" reply-to-message-id
                                "reply_markup" reply-markup})))

(defn send-video-note
  "Send a video note.

  `options` include: :duration, :length, :disable-notification, :reply-to-message-id, and :reply-markup.
  (XXX: API returns 'Bad Request: wrong video note length' when length is not given / 2017.05.19.)

  (https://core.telegram.org/bots/api#sendvideonote)"
  [bot chat-id video-note & options]
  (let [{:keys [duration
                length
                disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendVideoNote" {"chat_id" chat-id
                                    "video_note" video-note
                                    "duration" duration
                                    "length" length
                                    "disable_notification" disable-notification
                                    "reply_to_message_id" reply-to-message-id
                                    "reply_markup" reply-markup})))

(defn send-media-group
  "Send a media group of photos or videos.

  `options` include: :disable-notification, and :reply-to_message-id.

  (https://core.telegram.org/bots/api#sendmediagroup)"
  [bot chat-id media & options]
  (let [{:keys [disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendMediaGroup" {"chat_id" chat-id
                                     "media" media
                                     "disable_notification" disable-notification
                                     "reply_to_message_id" reply-to-message-id})))

(defn send-location
  "Send a location.

  `options` include: :disable-notification, :reply-to-message-id, and :reply-markup.

  (https://core.telegram.org/bots/api#sendlocation)"
  [bot chat-id latitude longitude & options]
  (let [{:keys [disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendLocation" {"chat_id" chat-id
                                   "latitude" latitude
                                   "longitude" longitude
                                   "disable_notification" disable-notification
                                   "reply_to_message_id" reply-to-message-id
                                   "reply_markup" reply-markup})))

(defn send-venue
  "Send a venue.

  `options` include: :foursquare-id, :foursquare-type, :disable-notification, :reply-to-message-id, and :reply-markup.

  (https://core.telegram.org/bots/api#sendvenue)"
  [bot chat-id latitude longitude title address & options]
  (let [{:keys [foursquare-id
                foursquare-type
                disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendVenue" {"chat_id" chat-id
                                "latitude" latitude
                                "longitude" longitude
                                "title" title
                                "address" address
                                "foursquare_id" foursquare-id
                                "foursquare_type" foursquare-type
                                "disable_notification" disable-notification
                                "reply_to_message_id" reply-to-message-id
                                "reply_markup" reply-markup})))

(defn send-contact
  "Send a contact.

  `options` include: :last-name, :vcard, :disable-notification, :reply-to-message-id, and :reply-markup.

  (https://core.telegram.org/bots/api#sendcontact)"
  [bot chat-id phone-number first-name & options]
  (let [{:keys [last-name
                vcard
                disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendContact" {"chat_id" chat-id
                                  "phone_number" phone-number
                                  "first_name" first-name
                                  "last_name" last-name
                                  "vcard" vcard
                                  "disable_notification" disable-notification
                                  "reply_to_message_id" reply-to-message-id
                                  "reply_markup" reply-markup})))

(defn send-poll
  "Send a poll.

  `options` include: :disable-notification, :reply-to-message-id, and :reply-markup.

  (https://core.telegram.org/bots/api#sendpoll)"
  [bot chat-id question poll-options & options]
  (let [{:keys [disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendPoll" {"chat_id" chat-id
                               "question" question
                               "options" poll-options
                               "disable_notification" disable-notification
                               "reply_to_message_id" reply-to-message-id
                               "reply_markup" reply-markup})))

(defn stop-poll
  "Stop a poll.

  `options` include: :reply_markup.

  (https://core.telegram.org/bots/api#stoppoll)"
  [bot chat-id message-id & options]
  (let [{:keys [reply-markup]} options]
    (h/request bot "stopPoll" {"chat_id" chat-id
                               "message_id" message-id
                               "reply_markup" reply-markup})))

(defn send-chat-action
  "Send a chat action.

  `action` can be one of: :typing, :upload_photo, :record_video, :upload_video, :record_audio, :upload_audio, :upload_document, :find_location, :record_video_note, or :upload_video_note.

  (https://core.telegram.org/bots/api#sendchataction)"
  [bot chat-id action]
  (h/request bot "sendChatAction" {"chat_id" chat-id
                                   "action" action}))

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

(defn get-file-url
  "Generate a file's url from given :file_path."
  [bot file-path]
  (h/url-for-filepath bot file-path))

(defn get-file
  "Fetch a file's info.

  (https://core.telegram.org/bots/api#getfile)"
  [bot file-id]
  (let [result (h/request bot "getFile" {"file_id" file-id})]
    (if (:ok result)
      (assoc-in result [:result :url] (get-file-url bot (get-in result [:result :file_path])))
      result)))

(defn kick-chat-member
  "Kick a chat member.

  `options` include: :until-date

  (https://core.telegram.org/bots/api#kickchatmember)"
  [bot chat-id user-id & options]
  (let [{:keys [until-date]} options]
    (h/request bot "kickChatMember" {"chat_id" chat-id
                                     "user_id" user-id
                                     "until_date" until-date})))

(defn leave-chat
  "Leave a chat.

  (https://core.telegram.org/bots/api#leavechat)"
  [bot chat-id]
  (h/request bot "leaveChat" {"chat_id" chat-id}))

(defn unban-chat-member
  "Unban a chat member.

  (https://core.telegram.org/bots/api#unbanchatmember)"
  [bot chat-id user-id]
  (h/request bot "unbanChatMember" {"chat_id" chat-id
                                    "user_id" user-id}))

(defn restrict-chat-member
  "Restrict a chat member.

  `options` include: :can-send-messages, :can-send-media-messages, :can-send-polls, :can-send-other-messages, :can-add-web-page-previews, :can-change-info, :can-invite-users, :can-pin-messages, and :until-date.

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
                until-date]
         :or {can-send-messages false
              can-send-media-messages false
              can-send-polls false
              can-send-other-messages false
              can-add-web-page-previews false
              can-change-info false
              can-invite-users false
              can-pin-messages false}} options]
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
                                         "until_date" until-date})))

(defn promote-chat-member
  "Promote a chat member.

  `options` include: :can-change-info, :can-post-messages, :can-edit-messages, :can-delete-messages, :can-invite-users, :can-restrict-members, :can-pin-messages, and :can-promote-members.

  (https://core.telegram.org/bots/api#promotechatmember)"
  [bot chat-id user-id & options]
  (let [{:keys [can-change-info
                can-post-messages
                can-edit-messages
                can-delete-messages
                can-invite-users
                can-restrict-members
                can-pin-messages
                can-promote-members]} options]
    (h/request bot "promoteChatMember" {"chat_id" chat-id
                                        "user_id" user-id
                                        "can_change_info" can-change-info
                                        "can_post_messages" can-post-messages
                                        "can_edit_messages" can-edit-messages
                                        "can_delete_messages" can-delete-messages
                                        "can_invite_users" can-invite-users
                                        "can_restrict_members" can-restrict-members
                                        "can_pin_messages" can-pin-messages
                                        "can_promote_members" can-promote-members})))

(defn set-chat-permission
  "Set chat permissions.

  `options` include: :can-send-messages, :can-send-media-messages, :can-send-polls, :can-send-other-messages, :can-add-web-page-previews, :can-change-info, :can-invite-users, and :can-pin-messages.

  (https://core.telegram.org/bots/api#setchatpermissions)"
  [bot chat-id & options]
  (let [{:keys [can-send-messages
                can-send-media-messages
                can-send-polls
                can-send-other-messages
                can-add-web-page-previews
                can-change-info
                can-invite-users
                can-pin-messages]
         :or {can-send-messages false
              can-send-media-messages false
              can-send-polls false
              can-send-other-messages false
              can-add-web-page-previews false
              can-change-info false
              can-invite-users false
              can-pin-messages false}} options]
    (h/request bot "setChatPermission" {"chat_id" chat-id
                                        "permissions" {"can_send_messages" can-send-messages
                                                       "can_send_media_messages" can-send-media-messages
                                                       "can_send_polls" can-send-polls
                                                       "can_send_other_messages" can-send-other-messages
                                                       "can_add_web_page_previews" can-add-web-page-previews
                                                       "can_change_info" can-change-info
                                                       "can_invite_users" can-invite-users
                                                       "can_pin_messages" can-pin-messages}})))

(defn export-chat-invite-link
  "Export a chat invite link.

  (https://core.telegram.org/bots/api#exportchatinvitelink)"
  [bot chat-id]
  (h/request bot "exportChatInviteLink" {"chat_id" chat-id}))

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
  [bot chat-id]
  (h/request bot "unpinChatMessage" {"chat_id" chat-id}))

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

(defn get-chat-members-count
  "Fetch the count of chat members.

  (https://core.telegram.org/bots/api#getchatmemberscount)"
  [bot chat-id]
  (h/request bot "getChatMembersCount" {"chat_id" chat-id}))

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

(defn edit-message-text
  "Edit a message's text.

  required `options`: :chat-id + :message-id (when :inline-message-id is not given)
                      or :inline-message-id (when :chat-id & :message-id are not given)

  other `options` include: :parse-mode, :disable-web-page_preview, and :reply-markup.

  (https://core.telegram.org/bots/api#editmessagetext)"
  [bot text & options]
  (let [{:keys [chat-id
                message-id
                inline-message-id
                parse-mode
                disable-web-page-preview
                reply-markup]} options]
    (h/request bot "editMessageText" {"text" text
                                      "chat_id" chat-id
                                      "message_id" message-id
                                      "inline_message_id" inline-message-id
                                      "parse_mode" parse-mode
                                      "disable_web_page_preview" disable-web-page-preview
                                      "reply_markup" reply-markup})))

(defn edit-message-caption
  "Edit a message's caption.

  required `options`: :chat-id + :message-id (when :inline-message-id is not given)
                      or :inline-message-id (when :chat-id & :message-id are not given)

  other `options` include: :parse-mode, and :reply-markup.

  (https://core.telegram.org/bots/api#editmessagecaption)"
  [bot caption & options]
  (let [{:keys [chat-id
                message-id
                inline-message-id
                parse-mode
                reply-markup]} options]
    (h/request bot "editMessageCaption" {"caption" caption
                                         "chat_id" chat-id
                                         "message_id" message-id
                                         "inline_message_id" inline-message-id
                                         "parse_mode" parse-mode
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

  other `options` include: :reply-markup.

  (https://core.telegram.org/bots/api#editmessagelivelocation)"
  [bot latitude longitude & options]
  (let [{:keys [chat-id
                message-id
                inline-message-id
                reply-markup]} options]
    (h/request bot "editMessageLiveLocation" {"latitude" latitude
                                              "longitude" longitude
                                              "chat_id" chat-id
                                              "message_id" message-id
                                              "inline_message_id" inline-message-id
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

  `options` include: :provider-data, :photo-url, :photo-size, :photo-width, :photo-height, :need-name, :need-phone-number, :need-email, :need-shipping-address, :send-phone-number-to-provider, :send-email-to-provider, :is-flexible, :disable-notification, :reply-to-message-id and :reply-markup.

  (https://core.telegram.org/bots/api#sendinvoice)"
  [bot chat-id title description payload provider-token start-parameter currency prices & options]
  (let [{:keys [provider-data
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
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendInvoice" {"chat_id" chat-id
                                  "title" title
                                  "description" description
                                  "payload" payload
                                  "provider_token" provider-token
                                  "start_parameter" start-parameter
                                  "currency" currency
                                  "prices" prices
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
                                  "reply_to_message_id" reply-to-message-id
                                  "reply_markup" reply-markup})))

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
  (let [{:keys [shipping-options
                error-message]} options]
    (h/request bot "answerPreCheckoutQuery" {"pre_checkout_query_id" pre-checkout-query-id
                                             "ok" ok
                                             "error_message" error-message})))

(defn send-game
  "Send a game.

  `options` include: :disable-notification, :reply-to-message-id, and :reply-markup.

  (https://core.telegram.org/bots/api#sendgame)"
  [bot chat-id game-short-name & options]
  (let [{:keys [disable-notification
                reply-to-message-id
                reply-markup]} options]
    (h/request bot "sendGame" {"chat_id" chat-id
                               "game_short_name" game-short-name
                               "disable_notification" disable-notification
                               "reply_to_message_id" reply-to-message-id
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

