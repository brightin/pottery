# Pottery

A library to use [GNU Gettext](https://www.gnu.org/software/gettext/) as translation solution for clojure projects. This small library is meant to extract translatable strings from your codebase, generate a PO Template file and parsing PO (translation) files.

## Why Gettext

TODO

## Installation

Add this dependency to your project:

``` clojure
[brightin/pottery "0.0.1"]
```

And require it:

``` clojure
(require '[pottery.core :as pottery])
```

## Basic usage

### 0. Define your translation function

This library is only meant to generate and read PO files. Translating in and of itself is up to you and your favorite library.

``` clojure
(defn tr [s & args]
  (translate-string-to-current-language s args))
```

Throughout your code this `tr` function will be used, and now Pottery will manage the gettext part.

### 1. Scan your codebase

Call the scan function from the REPL:

``` clojure
(pottery/scan-codebase!)
```

This will scan the codebase for any translatable strings and write the PO template file to `resources/gettext/template.pot`.

See below for more configurations of the scanner.

### 2. Use tools to generate translation files

For every string in the codebase there needs to be a translation to other languages. These get written in `<lang>.po` files. There are various tools available to achieve this. The most popular and recomended one would the free GUI tool [Poedit](https://poedit.net/). Use Poedit to load the PO template file and generate the translation files, and interactively translate all the strings.

TODO: screenshot

### 3. Parse the translation files

With those translation files generated from step 2, Pottery can scan those files into a map from `{"Original String" "Translated string"}`.

``` clojure
(pottery/read-po-file (io/resource "gettext/<lang>.po"))
=> {"Original string" "Translated string in <lang>"}
```

### 4. Repeat!

Whenever strings change in the codebase, at will re-scan c.f. step 1. This will replace the old template file, ensuring changed strings are changed, and removed strings are removed. Then merge that new template file in the translation files from step 2. In Poedit you can achieve this by going to *Catalog -> Update from POT file*. When you save in Poedit you can also purge deleted strings from the po file via *Catalog -> Purge Deleted Translations*. You translation files are now in sync!

## Gettext features

This library supports most features that gettext supports. More will be added at request, feel free to create an issue for them.

See "Extra features" below on how to configure the scanner to work with your project. The defaults are such that:

### Simple string

In your code:

``` clojure
(tr "Hello %s" name)
```

The output after parsing a PO file:

``` clojure
{"Hello %s" "Hoi %s")
```

### Pluralisation

In your code:

``` clojure
(trn ["One horse" "%s horses"] horse-count)
```

The output after parsing a PO file:

``` clojure
{["One horse" "%s horses"] ["Een paard" "%s paarden"]}
```

### Context _NOT IMPLEMENTED_

Sometimes the same string can have multiple translations according to context. Gettext has support for this as a translation context. This has not been implemented yet, please submit an issue if the need arises.

### Translator notes

Translator notes are great to provide context for the string to be translated. Since we defer development time and translation time, there might be confusion due to the lack of context. You can add notes to strings as metadata:

``` clojure
^{:notes "Abbreviation of horsepower, not healthpoints"}
(tr "Hp")
```

These translations will be extracted and added to the template file. At translation time these will be visible in, for example, Poedit:

TODO: screenshot.

#### Extract fn

The default extractor works as follows:

``` clojure
(tr ["My string" & args])                => "My string"
(trn ["Singular" "Plural" & args] count) => ["Singular" "Plural"]
```


## License

Copyright © 2019 Brightin

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
