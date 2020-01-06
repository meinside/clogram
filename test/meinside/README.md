# How to test

## Generate a bot token

You need to create your own telegram bot and generate a token with [this guide](https://core.telegram.org/bots#3-how-do-i-create-a-bot).

## Start a group chat with your bot

Create a group chat and invite your bot to it, then send some messages.

## Retrieve the chat's id

You need a chat id for further testing, so retrieve it using nREPL:

```clojure
meinside.clogram=> (require '[meinside.clogram :as cg])
nil

;; create a bot with your token
meinside.clogram=> (def bot (cg/new-bot "your-telegram-bot-token-here"))
#'meinside.clogram/bot

;; then fetch updates from your bot
meinside.clogram=> (cg/get-updates bot)
{:ok true
 :result [{:message {:chat {:all-members-are-administrators true
                            :id -123456
                            :title "Group Chat for Test"
                            :type "group"}
                     :date 1577836800
                     :from {:first-name "Firstname"
                            :id 123456
                            :is-bot false
                            :language-code "en"
                            :last-name "Lastname"
                            :username "username"}
                     :message-id 123
                     :text "test message"}
           :update-id 1234567}]}

meinside.clogram=>
```

You can see the chat id from the fetched results.

(`-123456` in this example)

## Run tests with the token and chat id

Run `lein test` with following environment variables (put yours):

```bash
$ TOKEN=your-telegram-bot-token-here CHAT_ID=-123456 lein test
```

If you want to see verbose log messages, append `VERBOSE=true`:

```bash
$ TOKEN=your-telegram-bot-token-here CHAT_ID=-123456 VERBOSE=true lein test
```

