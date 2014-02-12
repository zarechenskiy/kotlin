// FILE: fileDependencyRecursion.kt
package test

import testOther.some
import testOther.A

val normal: A = A()
val fromImported: A = some

// FILE: fileDependencyRecursionOther.kt
package testOther

import test.normal

val some: A = A()
val fromImported: A = normal

class A