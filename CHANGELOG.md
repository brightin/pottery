# Change Log

All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [1.0.2]

- Bugfix: Handle multiline notes correctly in .pot file
- Bugfix: Escape double quotes in msgid entries

## [1.0.1]

- Bugfix: Avoid slurping directories. https://github.com/brightin/pottery/pull/10

## [1.0.0]

- Bugfix: parse keys destructuring with auto-aliasing
- Bugfix: some expressions in `.cljc` go undetected

## [0.0.5]

- Bugfix: Allow multiline message ids. https://github.com/brightin/pottery/pull/2

## [0.0.4]

### Changed

- Added default-data-reader-fn binding to read-string so it can handle unknown tags

## [0.0.3]

### Added

- Added `read-po-str` to public interface of `pottery.core`

### Changed

- Updated license for Clojars

## [0.0.2]

### Added

- Added `pottery.po/read-po-str`. https://github.com/brightin/pottery/pull/1

### Changed

- License from EPL to Hippocratic License
- Default extractor now has string guards. Will warn when calling translation functions with the correct pattern but wrong types.

## [0.0.1]

### Added

- First version of Pottery.
