# Yamilify
### A simple YAML parser designed for easy integration into language and execution environments(LAE). 
- Provides a straightforward interface for loading and manipulating YAML data.
- Uses reflection to map YAML content to domain classes.
- Supports extensibility with custom parsers.
- Offers a benchmarking suite to evaluate performance.

### 1.1
The `YamlParserReflect` class is responsible for mapping YAML content to domain classes using reflection. 
To demonstrate how it works, consider the following example:

```kotlin
class Person(val name: String, val age: Int)

fun main() {
    val yaml = """
        name: Alice
        age: 30
    """.trimIndent()

    val person = YamlParserReflect().parse(yaml, Person::class)
    println(person)
}
```

In this example, the `YamlParserReflect` class parses the specified YAML content and maps it to an instance of the `Person` class.
The resulting `person` object contains the values extracted from the YAML content.

### 1.2
Properties of the domain class should be able to have names different from those used in the YAML representation.
For example, a property in YAML may have the name `city of birth`, while in Kotlin, it might be named `from`.
To address the mapping between properties with distinct names, implement a `YamlArg` annotation that can be used on the parameters of a domain class constructor
indicating the corresponding name in YAML (e.g., `@YamlArg("city of birth")`).
Modify `YamlParserReflect` to support the specified behavior and **validate it with unit tests.**

### 1.3
The `YamlParserReflect` should support extensibility with custom parsers provided by the domain class.
For instance, when dealing with a YAML mapping like `birth: 2004-05-26`, you might want to parse the value as an instance of `LocalDate`.
To achieve this, you can annotate the corresponding constructor argument as follows:

```kotlin
class Student(..., @YamlConvert(YamlToDate::class) val birth: LocalDate, ...)
```

In this example, `YamlToDate` is a class provided by the same developer that implements the domain class `Student`.

Adjust your implementation of `YamlParserReflect` to seamlessly integrate any parser provided by its clients for any data type.
A parser should be a class that offers a function taking a `String` argument and returning an `Object`.

Validate your implementation with the provided example of the `birth` property and create another example to showcase your approach with a different property type.

### 2.1
The `YamlParserCojen` class is an alternative implementation of the YAML parser that uses the Cojen bytecode generation library.
This implementation aims to provide a more efficient way of parsing YAML content by generating bytecode for the mapping logic.
To demonstrate how it works, consider the following example:

```kotlin
class Person(val name: String, val age: Int)
fun main() {
    val yaml = """
        name: Alice
        age: 30
    """.trimIndent()
    val person = YamlParserCojen.yamlParser(Person::class, 2).parseObject(yaml.reader())
    println(person)
}
```

### Usage of JMH

To run the benchmark on your local machine, run:

```
./gradlew jmhJar
```

Then:

```
java -jar yamlify-bench/build/libs/yamlify-bench-jmh.jar -i 4 -wi 4 -f 1 -r 2 -w 2 -tu ms
```

* `-i`  4 iterations
* `-wi` 4 warmup iterations
* `-f`  1 fork
* `-r`  2 run each iteration for 2 seconds
* `-w`  2 run each warmup iteration for 2 seconds.
* `-tu` ms time unit

### example of benchmark results:
```
Benchmark                                    Mode  Cnt    Score    Error   Units
YamlParserAccountBenchmark.accountBaseline  thrpt    4  773.368 ± 37.453  ops/ms
YamlParserAccountBenchmark.accountCojen     thrpt    4  604.398 ± 17.661  ops/ms
YamlParserAccountBenchmark.accountReflect   thrpt    4  509.808 ± 13.718  ops/ms
YamlParserStudentBenchmark.studentBaseline  thrpt    4  357.206 ± 17.028  ops/ms
YamlParserStudentBenchmark.studentCojen     thrpt    4  251.994 ±  4.371  ops/ms
YamlParserStudentBenchmark.studentReflect   thrpt    4  216.314 ±  7.414  ops/ms
```