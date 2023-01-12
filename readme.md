# Kotools Types

[![Kotools Types](https://img.shields.io/maven-central/v/io.github.kotools/types?label=latest)](https://search.maven.org/artifact/io.github.kotools/types)
[![Kotlin](https://img.shields.io/badge/kotlin-1.5.32-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![kotlinx.serialization](https://img.shields.io/badge/kotlinx.serialization-1.3.1-blue)](https://github.com/Kotlin/kotlinx.serialization)

This multiplatform library provides explicit types like `NonZeroInt` or
`NotBlankString`.

```kotlin
data class Person(val name: NotBlankString, val age: StrictlyPositiveInt)

fun main() {
    val name: NotBlankString = "Somebody".toNotBlankString().getOrThrow()
    val age: StrictlyPositiveInt = 42.toStrictlyPositiveInt().getOrThrow()
    val somebody = Person(name, age)
    println(somebody) // Person(name=Somebody, age=42)
}
```

## Design goals

### Less is more

Kotools Types focus primarily on what is essential for building explicit and
safer APIs: the types and their builder.
By having this minimalist approach, we engage to provide what users really need.
But as a user of this library, feel free to contribute to its evolution through
issues and pull requests (see the contribution section below).

### Avoid useless dependencies

Kotools Types is very light and just ship with one dependency:
[Kotlin/kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
for serializing or deserializing the provided types.
Knowing that these types could be used in any type of API, this feature is
essential for this library.

### Error handling agnostic

Users should be responsible for deciding how to handle errors, not this library.
Externalizing this responsibility to users implies that Kotools Types should
provide an explicit API by definition.
This is why we are using the
[`Result`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/) type
from Kotlin for returning a result that can be a success or a failure.

## Installation

### Gradle

#### Kotlin DSL

```kotlin
implementation("io.github.kotools:types:4.0.0")
```

#### Groovy DSL

```groovy
implementation "io.github.kotools:types:4.0.0"
```

### Maven

```xml
<dependencies>
    <dependency>
        <groupId>io.github.kotools</groupId>
        <artifactId>types</artifactId>
        <version>4.0.0</version>
    </dependency>
</dependencies>
```

## Contributing

Feel free to contribute to this project with
[issues](https://github.com/kotools/types/issues) and
[pull requests](https://github.com/kotools/types/pulls).

This project follows the [Conventional Commits][conventional-commits] guidelines
for committing with Git.
Please read [the specifications][conventional-commits] before committing to this
project.

[conventional-commits]: https://www.conventionalcommits.org/en/v1.0.0

## License

This project is licensed under the
[MIT License](https://choosealicense.com/licenses/mit).
