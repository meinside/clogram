# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [0.34.0] - 2025-04-14
- Apply API changes(https://core.telegram.org/bots/api-changelog#april-11-2025).

## [0.33.0] - 2025-02-13
- Apply API changes(https://core.telegram.org/bots/api-changelog#february-12-2025).

## [0.32.0] - 2025-01-02
- Apply API changes(https://core.telegram.org/bots/api-changelog#january-1-2025).

## [0.31.0] - 2024-11-18
- Apply API changes(https://core.telegram.org/bots/api#november-17-2024).

## [0.30.0] - 2024-11-06
- Apply API changes(https://core.telegram.org/bots/api-changelog#october-31-2024).
- Update versions of dependencies.

## [0.29.0] - 2024-09-09
- Apply API changes(https://core.telegram.org/bots/api-changelog#september-6-2024).

## [0.28.0] - 2024-08-16
- Apply API changes(https://core.telegram.org/bots/api-changelog#august-14-2024).

## [0.27.0] - 2024-08-01
- Apply API changes(https://core.telegram.org/bots/api-changelog#july-31-2024).

## [0.26.0] - 2024-07-02
- Apply API changes(https://core.telegram.org/bots/api#july-1-2024).

## [0.25.0] - 2024-06-20
- Apply API changes(https://core.telegram.org/bots/api#june-18-2024).

## [0.24.0] - 2024-05-29
- Apply API changes(https://core.telegram.org/bots/api#may-28-2024).

## [0.23.0] - 2024-05-07
- Apply API changes(https://core.telegram.org/bots/api#may-6-2024).

## [0.22.0] - 2024-04-01
- Apply API changes(https://core.telegram.org/bots/api#march-31-2024).

## [0.21.0] - 2024-01-03
- Apply API changes(https://core.telegram.org/bots/api#december-29-2023).

## [0.20.0] - 2023-09-25
- Apply API changes(https://core.telegram.org/bots/api-changelog#september-22-2023).

## [0.19.0] - 2023-08-21
- Apply API changes(https://core.telegram.org/bots/api-changelog#august-18-2023).

## [0.18.0] - 2023-04-24
- Apply API changes(https://core.telegram.org/bots/api-changelog#april-21-2023).
  - Add `get-my-name` and `set-my-name` functions.

## [0.17.0] - 2023-03-10
- Apply API changes(https://core.telegram.org/bots/api-changelog#march-9-2023).

## [0.16.0] - 2023-02-06
- Apply API changes(https://core.telegram.org/bots/api-changelog#february-3-2023).

## [0.15.0] - 2023-01-02
### Changed
- Add parameter :drop-pending-updates to `delete-webhook` function.
- Apply API changes(https://core.telegram.org/bots/api-changelog#december-30-2022).

## [0.14.0] - 2022-11-07
- Apply API changes(https://core.telegram.org/bots/api-changelog#november-5-2022).

## [0.13.1] - 2022-10-05
### Changed
- Edit tests.
- Hide `get-file-url` function.

## [0.13.0] - 2022-08-16
### Added
- Apply API changes(https://core.telegram.org/bots/api#august-12-2022).
  - Add function `get-custom-emoji-stickers`.

## [0.12.0] - 2022-06-21
### Added
- Apply API changes(https://core.telegram.org/bots/api-changelog#june-20-2022).
  - Add function `create-invoice-link`.

## [0.11.0] - 2022-04-18
- Apply API changes(https://core.telegram.org/bots/api-changelog#april-16-2022).

## [0.10.0] - 2022-02-03
### Changed
- Apply API changes(https://core.telegram.org/bots/api-changelog#january-31-2022).
  - Add parameter `webm_sticker` to functions: `create-new-sticker-set` and `add-sticker-to-set`.

## [0.9.0] - 2022-01-03
### Changed
- Apply API changes(https://core.telegram.org/bots/api-changelog#december-30-2021).
  - Add parameter `protect_content` to functions: `send-message`, `send-photo`, `send-video`, `send-animation`, `send-audio`, `send-document`, `send-sticker`, `send-video-note`, `send-voice`, `send-location`, `send-venue`, `send-contact`, `send-poll`, `send-dice`, `send-invoice`, `send-game`, `send-media-group`, `copy-message`, and `forward-message`.

## [0.8.0] - 2021-12-13
### Added
- Apply API changes(https://core.telegram.org/bots/api#december-7-2021).
  - Add functions `ban-chat-sender-chat` and `unban-chat-sender-chat`.

## [0.7.0] - 2021-11-08
### Added
- Apply API changes(https://core.telegram.org/bots/api#november-5-2021).
  - Add functions `approve-chat-join-request` and `decline-chat-join-request`.

### Changed
- Apply API changes(https://core.telegram.org/bots/api#november-5-2021).
  - Add params `name` and `creates-join-request` to functions `create-chat-invite-link` and `edit-chat-invite-link`.

## [0.6.0] - 2021-06-28
### Added
- Apply API changes(https://core.telegram.org/bots/api#june-25-2021).
  - Add function `delete-my-commands`.

### Changed
- Apply API changes(https://core.telegram.org/bots/api#june-25-2021).
  - Rename function `kick-chat-member` to `ban-chat-member`.
  - Rename function `get-chat-members-count` to `get-chat-member-count`.
  - Add optional parameters to function `get-my-commands`.
  - Add optional parameters to function `set-my-commands`.

## [0.5.0] - 2021-04-27
### Changed
- Apply API changes(https://core.telegram.org/bots/api#april-26-2021):
  - Add/change parameters of `send-invoice` function.
  - Fix parameters of `send-chat-action` function.

## [0.4.0] - 2021-03-10
### Added
- Add `create-chat-invite-link`, `edit-chat-invite-link`, and `revoke-chat-invite-link` functions. (https://core.telegram.org/bots/api-changelog#march-9-2021)

### Changed
- Change parameters for `kick-chat-member` and `promote-chat-member`. (https://core.telegram.org/bots/api-changelog#march-9-2021)

## [0.3.0] - 2020-11-05
### Added
- Add `copy-message`, and `unpin-all-chat-messages` function. (https://core.telegram.org/bots/api#november-4-2020)

### Changed
- Change `send-message`, `send-photo`, `send-audio`, `send-document`, `send-sticker`, `send-video`, `send-animation`, `send-voice`, `send-video-note`, `send-media-group`, `send-location`, `send-venue`, `send-contact`, `send-poll`, `send-dice`, `unban-chat-member`, `promote-chat-member`, `unpin-chat-message`, `edit-message-text`, `edit-message-caption`, `edit-message-live-location`, `send-invoice`, and `send-game` function signatures.

## [0.2.3] - 2020-04-24
### Changed
- Add params to `send-poll` and `send-dice` functions. (https://core.telegram.org/bots/api-changelog#april-24-2020)

## [0.2.2] - 2020-03-31
### Added
- Add `send-dice`, `get-my-commands`, `set-my-commands`, and `set-sticker-set-thumb` functions. (https://core.telegram.org/bots/api#march-30-2020)

### Changed
- Change `create-new-sticker-set`, `add-sticker-to-set` function signatures.

## [0.2.1] - 2020-01-29
### Changed
- Add params to `send-poll` function. (https://core.telegram.org/bots/api-changelog#january-23-2020)

## [0.2.0] - 2020-01-15
### Added
- Add support for ClojureScript.
- Add some test cases for ClojureScript.

## [0.1.2] - 2020-01-06
### Added
- Add an environment variable (VERBOSE) for testing.
- Add a guide file (README.md) for testing.

## [0.1.1] - 2020-01-02
### Added
- Add `set-chat-administrator-custom-title` function. (https://core.telegram.org/bots/api-changelog#december-31-2019)

## [0.1.0] - 2019-12-19
### Added
- Add tests for long-polling functions.

### Changed
- Change long-polling functions to return boolean values.

### Removed
- Remove :jar-exclusions from project.clj.

## [0.0.10], [0.0.11], [0.0.12] - 2019-12-19
### Fixed
- Fix for cljdoc and excluding files from jar.

## [0.0.9] - 2019-12-17
### Fixed
- Lint codes.

## [0.0.8] - 2019-12-17
### Changed
- Convert returning jsons' keys from snake to kebab case.

### Fixed
- Fix typos in comments.

## [0.0.7] - 2019-12-16
### Changed
- Apply clojure style guide to some codes and comments (https://github.com/bbatsov/clojure-style-guide)

## [0.0.6] - 2019-12-16
### Added
- Add more test codes. (WIP)

### Changed
- Rename `stop-polling` to `stop-polling-updates` for clarity.

## [0.0.5] - 2019-12-13
### Added
- Add some more test codes. (WIP)

## [0.0.4] - 2019-12-13
### Added
- Add some test codes. (WIP)

### Changed
- Change `stop-polling` function to return a boolean value.

## [0.0.3] - 2019-12-13
### Fixed
- Fix typo: `edit-message-caption` to `edit-message-media`.

### Changed
- Reorganize files for tests. (WIP)

## [0.0.2] - 2019-12-12
### Changed
- Fix not to wait for returned channel when polling updates.
- Edit guides in README.md.

## [0.0.1] - 2019-12-09
### Added
- Add functions for long-polling updates.

### Changed
- Reorganize files.
- Add timestamp to log functions.
- Edit guides in README.md.

## [0.0.0] - 2019-12-09
### Added
- Initial commit.

[0.34.0]: https://github.com/meinside/clogram/compare/v0.33.0...v0.34.0
[0.33.0]: https://github.com/meinside/clogram/compare/v0.32.0...v0.33.0
[0.32.0]: https://github.com/meinside/clogram/compare/v0.31.0...v0.32.0
[0.31.0]: https://github.com/meinside/clogram/compare/v0.30.0...v0.31.0
[0.30.0]: https://github.com/meinside/clogram/compare/v0.29.0...v0.30.0
[0.29.0]: https://github.com/meinside/clogram/compare/v0.28.0...v0.29.0
[0.28.0]: https://github.com/meinside/clogram/compare/v0.27.0...v0.28.0
[0.27.0]: https://github.com/meinside/clogram/compare/v0.26.0...v0.27.0
[0.26.0]: https://github.com/meinside/clogram/compare/v0.25.0...v0.26.0
[0.25.0]: https://github.com/meinside/clogram/compare/v0.24.0...v0.25.0
[0.24.0]: https://github.com/meinside/clogram/compare/v0.23.0...v0.24.0
[0.23.0]: https://github.com/meinside/clogram/compare/v0.22.0...v0.23.0
[0.22.0]: https://github.com/meinside/clogram/compare/v0.21.0...v0.22.0
[0.21.0]: https://github.com/meinside/clogram/compare/v0.20.0...v0.21.0
[0.20.0]: https://github.com/meinside/clogram/compare/v0.19.0...v0.20.0
[0.19.0]: https://github.com/meinside/clogram/compare/v0.18.0...v0.19.0
[0.18.0]: https://github.com/meinside/clogram/compare/v0.17.0...v0.18.0
[0.17.0]: https://github.com/meinside/clogram/compare/v0.16.0...v0.17.0
[0.16.0]: https://github.com/meinside/clogram/compare/v0.15.0...v0.16.0
[0.15.0]: https://github.com/meinside/clogram/compare/v0.14.0...v0.15.0
[0.14.0]: https://github.com/meinside/clogram/compare/v0.13.1...v0.14.0
[0.13.1]: https://github.com/meinside/clogram/compare/v0.13.0...v0.13.1
[0.13.0]: https://github.com/meinside/clogram/compare/v0.12.0...v0.13.0
[0.12.0]: https://github.com/meinside/clogram/compare/v0.11.0...v0.12.0
[0.11.0]: https://github.com/meinside/clogram/compare/v0.10.0...v0.11.0
[0.10.0]: https://github.com/meinside/clogram/compare/v0.9.0...v0.10.0
[0.9.0]: https://github.com/meinside/clogram/compare/v0.8.0...v0.9.0
[0.8.0]: https://github.com/meinside/clogram/compare/v0.7.0...v0.8.0
[0.7.0]: https://github.com/meinside/clogram/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/meinside/clogram/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/meinside/clogram/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/meinside/clogram/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/meinside/clogram/compare/v0.2.3...v0.3.0
[0.2.3]: https://github.com/meinside/clogram/compare/v0.2.2...v0.2.3
[0.2.2]: https://github.com/meinside/clogram/compare/v0.2.1...v0.2.2
[0.2.1]: https://github.com/meinside/clogram/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/meinside/clogram/compare/v0.1.2...v0.2.0
[0.1.2]: https://github.com/meinside/clogram/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/meinside/clogram/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/meinside/clogram/compare/v0.0.12...v0.1.0
[0.0.12]: https://github.com/meinside/clogram/compare/v0.0.11...v0.0.12
[0.0.11]: https://github.com/meinside/clogram/compare/v0.0.10...v0.0.11
[0.0.10]: https://github.com/meinside/clogram/compare/v0.0.9...v0.0.10
[0.0.9]: https://github.com/meinside/clogram/compare/v0.0.8...v0.0.9
[0.0.8]: https://github.com/meinside/clogram/compare/v0.0.7...v0.0.8
[0.0.7]: https://github.com/meinside/clogram/compare/v0.0.6...v0.0.7
[0.0.6]: https://github.com/meinside/clogram/compare/v0.0.5...v0.0.6
[0.0.5]: https://github.com/meinside/clogram/compare/v0.0.4...v0.0.5
[0.0.4]: https://github.com/meinside/clogram/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/meinside/clogram/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/meinside/clogram/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/meinside/clogram/compare/v0.0.0...v0.0.1
[0.0.0]: https://github.com/meinside/clogram/releases/tag/v0.0.0

