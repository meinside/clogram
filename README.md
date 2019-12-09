# clogram

A Clojure library for Telegram Bot API.

## Usage

### Interactively

```
(require '[clogram.bot :as cg])

;; generate your bot token with this guide: https://core.telegram.org/bots#3-how-do-i-create-a-bot
(def bot-token "0123456789:abcdefghijklmnopqrstuvwxyz")

;; create a new bot
(def bot (cg/new-bot bot-token
                     :verbose? true))

;; get updates from your bot
(cg/get-update bot)

;; send 'typing...' to chat id: 123456
(cg/send-chat-action bot 123456 :typing)

;; send a message to chat id: 123456
(cg/send-message bot 123456 "this is a message from bot")
```

### Long-Polling

TODO - Add guides here.

### Using Webhook

TODO - Add guides here.

## Todo

- [ ] Add tests.
- [ ] Add functions for long-polling updates.
- [ ] Add functions for webhook.

## License

MIT

