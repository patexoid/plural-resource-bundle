# plural-resource-bundle

[![CircleCI](https://circleci.com/gh/patexoid/plural-resource-bundle.svg?style=shield)](https://circleci.com/gh/patexoid/plural-resource-bundle)

A thin wrapper around `java.util.ResourceBundle` that adds grammatical
plural-form support to message formatting — not just the English
one/many split. Ukrainian, English and Russian are supported out of the
box, and adding a new language only requires a pluralization function and
(optionally) a word list.

## Why

`java.text.MessageFormat`'s `choice` format only handles simple numeric
ranges, which isn't enough for languages like Ukrainian or Russian that
have three plural forms (e.g. `1 книга`, `2 книги`, `5 книг`). This
library adds a small `<p:word>` syntax on top of `MessageFormat` that
picks the correct grammatical form of a word based on a count argument
and the bundle's locale.

## Installation

Artifacts are published to GitHub Packages.

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/patexoid/repo")
        credentials {
            username = System.getenv("USERNAME")
            password = System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("com.patex:plural-resource-bundle:<version>")
}
```

GitHub Packages requires authentication even for public read access — see
[GitHub's docs](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)
for configuring `USERNAME`/`TOKEN`.

## Usage

Wrap a standard `ResourceBundle` with `PluralResourceBundle` and use
`get(key, args...)` instead of `getString(key)`:

```java
ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
PluralResourceBundle pluralBundle = new PluralResourceBundle(bundle);

pluralBundle.get("greeting", 1); // "1 book"
pluralBundle.get("greeting", 2); // "2 books"
```

Where `messages.properties` contains:

```properties
greeting={0} <p:book>
```

The `<p:word>` marker is replaced with the correct plural form of
`word`, chosen from the argument at the *nearest preceding* `{index}`
placeholder — or an explicit `{index}` placed right after `p:`:

```properties
# uses the argument that comes right before it ({0})
inventory={0} <p:book> and {1} <p:sequence>

# or reference an argument explicitly
inventory={1} <p:{0}book>
```

The rest of the string is still processed by `java.text.MessageFormat`,
so all its placeholder features (`{0}`, `{1}`, etc.) work as usual.
Capitalization of the first letter of the matched word is preserved:
`<p:Book>` with count `2` renders as `Books`.

### Registering word forms

Word forms are registered via resource files: drop a `plural_<lang>.txt`
file on the classpath (e.g. `plural_ru.txt`), one comma-separated word
per line, ordered by plural-form index for that language:

```
книга,книги,книг
серия,серии,серий
```

If a word has no registered forms, it's left unchanged.

### Supported languages

| Language  | Locale code | Forms                                   |
|-----------|-------------|------------------------------------------|
| English   | `en`        | singular / plural                        |
| Ukrainian | `uk`        | singular / few (2-4) / many              |
| Russian   | `ru`        | singular / few (2-4) / many              |
| Other     | any         | falls back to a single form (no changes) |

Adding a new language means providing a `count -> formIndex` function
(see `PluralChooserFactory`) and, optionally, a `plural_<lang>.txt` word
list.

## Building

```shell
./gradlew build
./gradlew test
```

## License

No license file is currently published with this project.
