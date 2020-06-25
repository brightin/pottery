# Pottery

A library to use [GNU Gettext](https://www.gnu.org/software/gettext/) as translation solution for clojure projects. This library is meant to extract translatable strings from your codebase (clj, cljs and cljc), generate a PO Template file and parsing PO (translation) files.

## Why Gettext

Gettext is an old but great solution for managing translatable codebases. In comparison to the i18n or similar ways:

- No need to invent unique keys anymore for every piece of text that needs translations. The string itself is the key.
- Codebase and translation files stay in sync via tooling, not developers. Since the string is the key, if the string changes, translations must follow.
- Defered development time vs translation time. When developing you don't want to have to manage localisation files. Gettext defers this process to a more appropriate time, like before releases for example. The developer just has to write text in the codebase in whatever language the team is comfortable with.
- Outsource translations easily to professional translators. PO and POT files are an industry standard for translation bureaus.

## Why make this library

There are some libraries in the wild that do some parts of what Pottery tries to achieve. However most of them either had an incomplete API, or not a functional one. Pottery is a functional approach to using gettext, leaving the developer in the front seat when translating his or her application.

## Installation

Add this dependency to your project:

```clojure
[brightin/pottery "0.0.1"]
```

And require it:

```clojure
(require '[pottery.core :as pottery])
```

## Basic usage

### 0. Define your translation function

This library is only meant to generate and read PO files. Translating in and of itself is up to you. There are plenty of great libraries out there for this with which Pottery has no intention to compete. There is a [full example](https://github.com/brightin/pottery/tree/master/examples/simple/example.clj) to demonstrate how this could work.

```clojure
(defn tr [s & args]
  (translate-string-to-current-language s args))
```

Throughout your code this `tr` function will be used, and now Pottery will manage the gettext part.

### 1. Scan your codebase

Call the scan function from the REPL:

```clojure
(pottery/scan-codebase!)
```

This will scan the codebase for any translatable strings and write the PO template file to `resources/gettext/template.pot`. This is not limited to clojure only files, strings in .cljc and .cljs files are also extracted.

See [Configuration / Extra features](#configuration--extra-features) for more configurations of the scanner.

### 2. Use tools to generate translation files

For every string in the codebase there needs to be a translation to other languages. These get written in `<lang>.po` files. There are various tools available to achieve this. The most popular and recomended one would the free GUI tool [Poedit](https://poedit.net/). Use Poedit to load the PO template file, generate the translation files and interactively translate all the strings.

![Poedit](/github/poedit.png?raw=true "Poedit GUI")

### 3. Parse the translation files

Pottery parses the translation files from step 2 into a dictionary with the form: `{"Original String" "Translated string"}`.

```clojure
(pottery/read-po-file (io/resource "gettext/es.po"))
=> {"Hello" "Hola"}
```

These can act as dictionaries for your translation function in step 0.

### 4. Repeat!

Whenever strings change in the codebase, re-scan at will as in step 1. This will replace the old template file, ensuring changed strings are changed, and removed strings are removed. Next merge that new template file in the translation files. In Poedit you can achieve this by going to _Catalog -> Update from POT file_. After saving in Poedit you can also purge deleted strings from the po file via _Catalog -> Purge Deleted Translations_. You translation files are now in sync!

## Gettext features

This library supports most features that gettext supports. More will be added at request, feel free to create an issue for them. The following examples use the default scanner:

### Simple string

In your code:

```clojure
(tr "Hello %s" name)
```

The output after parsing a PO file:

```clojure
{"Hello %s" "Hoi %s")
```

### Pluralisation

In your code:

```clojure
(trn ["One horse" "%s horses"] horse-count)
```

The output after parsing a PO file:

```clojure
{["One horse" "%s horses"] ["Een paard" "%s paarden"]}
```

### Context

_Not implemented yet_

Sometimes the same string can have multiple translations according to context. Gettext has support for this as a translation context. This has not been implemented yet, please submit an issue if the need arises.

### Translator notes

Translator notes are great to provide context for the string to be translated. Since we defer development time and translation time, there might be confusion due to the lack of context. You can add notes to strings as metadata:

```clojure
^{:notes "Abbreviation of horsepower, not healthpoints"}
(tr "Hp")
```

These translations will be extracted and added to the template file. At translation time these will be visible in, for example, Poedit:

![Poedit translator notes](/github/poedit_translator_notes.png?raw=true "Poedit translator notes")

## Configuration / Extra features

### 1. Scanning

The scan function takes a few options:

| Key            | Description                                                                  | Default                            |
| :------------- | :--------------------------------------------------------------------------- | ---------------------------------- |
| :dir           | The source directory which needs to be scanned                               | `"src"`                            |
| :template-file | The output template file                                                     | `"resources/gettext/template.pot"` |
| :extract-fn    | The function which maps any expression found to a string, strings or nothing | `pottery.scan/default-extractor`   |

#### extract-fn

A function which takes a clojure expression as data, and returns a string (single) or a vector of strings (plural).

A simple extractor would look like this:

```clojure
(defn my-extract-fn [expr]
  (when (and (list? expr)
             (= 'tr (first expr))
             (string? (second expr)))
    (second expr)))

(my-extract-fn '(tr "Some string")) => "Some string"
(my-extract-fn '(inc 12)) => nil
```

It may get tedious to write a good function when you have multiple translation functions that have multiple arities. Pottery offers a shorthand using [core.match](https://github.com/clojure/core.match) to declare patterns in which translation functions are called.

```clojure
(pottery/make-extractor
  ['tr (s :guard string?) & _] s
  ['tr [s & _]] s
  ['trn [s1 s2 & _]] [s1 s2]
  ...)
```

It's a good idea to also warn when extraction did not pass any of the patterns, as a safe guard. As last pattern of the match sequence, you can provide:

```clojure
(pottery/make-extractor
  ... patterns
  [(:or 'tr 'trn) & _] (pottery.scan/extraction-warning
                         "Translation function called but no string could be extracted:"))
```

When the expression starts with the familiar function call, but did not match any pattern the warning will be printed with the failing expression. It's a good idea to write patterns for common "mistakes".

The default extractor is defined as such:

```clojure
(make-extractor
   ['tr (s :guard string?) & _] s
   ['trn [(s1 :guard string?) (s2 :guard string?) & _] _] [s1 s2]
   [(:or 'tr 'trn) & _] (extraction-warning
                         "Could not extrapolate translation string for the form:"))
```

It's a minimal extractor, which will match these clojure forms:

```clojure
(tr "My string" & args)                  => "My string"
(trn ["Singular" "Plural" & args] count) => ["Singular" "Plural"]
(tr some-var)                            => nil ;; And a warning is printed
(trn ["String"])                         => nil ;; And a warning is printed
```

See [the full example](https://github.com/brightin/pottery/tree/master/examples/simple/example.clj) for a simple but complete example of integrating Pottery in your application.

## Clojurescript usage example

This next example shows an approach for compiling dictionaries into the
clojurescript source. We use a macro to read the po file and compile the
translation dictionary at compile time, which will result in the dictionary
being hardcoded in the source.

```clojure
;; src/app/tr.clj
(ns app.tr
  (:require [pottery.po :as po]))

(defmacro inline-dict [filename]
  (po/read-po-file filename))

;; src/app/frontend/tr.cljs
(ns app.frontend.tr
  (:require-macros [app.tr :refer [inline-dict]]))

(def DICT
  {:en (inline-dict "gettext/en.po")
   :it (inline-dict "gettext/it.po")})
```

### Automatic recompilation with shadow-cljs

If you're using [shadow-cljs](https://github.com/thheller/shadow-cljs) you can
leverage `shadow.resource/slurp-resource` to read the contents of the PO file
and also record the file as a recompilation dependency.

```clojure
;; src/app/tr.clj
(ns app.tr
  (:require [pottery.po :as po]
            [shadow.resource :as res]))

(defmacro inline-dict [filename]
  (po/read-po-str (res/slurp-resource &env filename)))
```

## Gotchas

With Pottery, translation is done as a function but the function call should be regarded as data. The scanner reads source code with the clojure reader to figure out which strings are to be translated. For example:

```clojure
;; Bad
(let [msg (if available? "Available" "Unavailable")]
  (tr msg))

;; Good
(if available?
  (tr "Available")
  (tr "Unavailable"))
```

In the first case only "msg" or nothing would get extracted. In the second case all strings are extracted.

## Credits

Thanks to [Carlo Sciolla](https://github.com/skuro) for coming up with the name of this project!

## License

Copyright © 2020 Brightin

This project uses the [Hippocratic License](https://firstdonoharm.dev/), and is
thus freely available to use for purposes that do not infringe on the United
Nations Universal Declaration of Human Rights.
