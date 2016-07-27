# Value Types

* **Type**: Design proposal
* **Author**: Mikhail Zarechenskiy
* **Contributors**: Andrey Breslav
* **Status**: Under consideration
* **Prototype**: In progress

## Feedback

Discussion of this proposal is held in [this issue]()(add link).

## Goals

* Support *value types*
* Support generics over value types through *specialization*
* Be consistent with [Project Valhalla](http://openjdk.java.net/projects/valhalla/)

## Use cases

* Unsigned arithmetic
* Types representing units of measure
* Achieve typesafety with zero allocation overhead

## Description

*Value type* is a new kind of type in Kotlin language which is borrowing most of the definition and
encapsulation machinery from classes and at the same time is allowing to avoid allocating
runtime objects.

## Limitation

Without enhancements to the existing JVM, Kotlin supports only limited version of value types.

Restrictions regarding generic specialization over value types:
* Built-in class ```Array``` is the only class which could be specialized
* Only ```inline``` functions with ```@Anyfied``` type parameters could be specialized
* Only inlinable parameters of ```function types``` could be specialized

See limitation of value classes

## Value classes

TODO: value classes

Outline:
* Value class can have only single ```val``` parameter - the underlying runtime representation
* Value class can inherit only interfaces
* Value class is final
* Semantics of ```==```
* Reference cast
* Value class cannot have ```init``` blocks
* Values and properties?
* Methods from ```Any```: ```equals```, ```hashCode```, ```toString```

### Limitations

This section provides concrete consequences of value classes limitation. All these checks are performed statically i.e. at the compile time.

* Value classes must have exactly one primary constructor parameter
``` kotlin
value class IntPair(val first: Int, val second: Int) // Error: Value class must have exactly one primary constructor parameter
```
``` kotlin
value class Empty() // Error: Value class must have exactly one primary constructor parameter
```

* Value classes must have only read-only property
``` kotlin
value class Mutable(var sample: Int) // Error: Value class primary constructor must have only immutable (val) parameter
```

* Value classes must initialize property in primary constructor
``` kotlin
value class Sample(base: Int) // Error: Value class primary constructor must have only property (val) parameter
```

* Value classes must be top-level or a member of statically accessible object
``` kotlin
class Foo {
  value class Bar(val first: Int) // Error: Value classes are only allowed on top level or in objects
}
```

* Value classes must be final
``` kotlin
open value class Foo(val v: Int) // Error: Modifier 'open' is incompatible with 'vaue'
```

* Value classes could not be abstract
``` kotlin
abstract value class Foo(val v: Int) // Error: Modifier 'abstract' is incompatible with 'vaue'
```

* Implement only interfaces

## Syntax

Existing functions could be written with the assumption that a type variable ```T``` can always be converted to ```Any?```.
This assumption is wrong when a type variable ```T``` instantiated with a value.

To express that a type variable of function could also take a *value type* it should be marked with an ```@Anyfied``` annotation.

``` kotlin
inline fun foo <@Anyfied T>(el: T) {}
```

## Semantics

### Language restrictions

For an ```@Anyfied``` type variable ```T``` the following constructs are restricted:
* Cannot convert ```null``` to a variable whose type is ```T```
* Cannot convert ```Array<T>``` to ```Array<out Any>```
* Cannot convert ```T``` to ```Any?```
* Cannot synchronize using an expression of type ```T``` as a lock

### Relationship with ```Any```
For an ```@Anyfied``` type variable ```T``` the following constructs are refined:
* Assignment of ```T``` to ```out Any?```
    * Accepted only through explicit boxing (by applying function ```T.boxed()```)
* Conversion of ```T``` to ```out Any?```
    * Accepted only through explicit boxing

Example:

``` kotlin
inline fun <@Anyfied T> foo(el: T) {
    val p: Any? = el // error: explicit boxing is needed
    bar(el) // error: explicit boxing is needed, bar accepts only references

    baz(el) // OK
}

fun <T> bar(reference: T) {}

inline fun <@Anyfied T> baz(referenceOrValue: T) {}
```

### Type checks and casts with arrays
For an ```@Anyfied``` type variable ```T```:
* Casting to an ```Array<T>```
    * ```Array<T>``` downgrades to ```Array<Any>``` only when ```T``` is a reference type
* Type checks (```is``` and ```!is``` operators)
    * ```is Array<T>```, ```is Array<vInt>```
    * Discuss: current solution is to use ```Array<*>.isArrayOf```.
        Introduce intrinsic? Allow constructs ```is Array<T>```? What means ```is Array<*>```?

### Variance

For an ```@Anyfied``` type variable ```T```:
* Fold ```Array<in T>``` and ```Array<out T>``` to ```Array<T>``` when ```T``` is a value type

### Reflection access

TODO

### Boxing

An explicit boxing of a value class is needed when:
* A value class is treated as another type
* A value class is treated as a reference type
* A value class is treated as a nullable type

Example

``` kotlin
value class VInt(val v: Int)

fun foo() {
    val v = VInt(0)

    val p: Any = v // error: value type is treated as another type
    val r: VInt? = v // error: value type is treated as a nullable type
    bar(v) // error: value type is treated as a reference type
}

fun <T> bar(el: T) {}
```

## Solution overview

## Open questions

* Comparison of ```@Anyfied``` type variable to ```null```
    * Reject such comparisons, use ```T.isNull()``` instead
    * Introduce ```T.default```. For references ```T.default``` is null.
* Type checks
* Boxing: explicit / implicit
## Background / useful links

* [State of the values](http://cr.openjdk.java.net/~jrose/values/values-0.html)
* [Classfile enhancements for generic specialization](http://cr.openjdk.java.net/~briangoetz/valhalla/eg-attachments/model3-01.html)
* [The valhalla-spec-experts archives](http://mail.openjdk.java.net/pipermail/valhalla-spec-experts/)