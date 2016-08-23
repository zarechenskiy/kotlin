// WITH_RUNTIME

// FILE: value.kt

value class uInt(val v: Int)

// FILE: foo.kt

fun foo() {
    val v = uInt(1)
}

// @FooKt.class:
// 0 ALOAD
// 0 ARETURN
// 0 ASTORE
// 0 valueOf
// 0 NEW
// 0 DUP
// 0 INVOKESPECIAL
// 1 ISTORE
