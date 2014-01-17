package test

var a = 1

// val prop1: null
val prop1 = a

// val prop2: null
val prop2 = a + 1

class A {
    // val prop3: null
    val prop3 = a

    // val prop4: null
    val prop4 = a + 1
}

fun foo() {
    // val prop5: null
    val prop5 = a

    // val prop6: null
    val prop6 = a + 1
}