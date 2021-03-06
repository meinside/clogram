# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

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

