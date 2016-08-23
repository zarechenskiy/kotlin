// WITH_RUNTIME

// FILE: library.kt

inline fun <@Anyfied T> test(t1: T, t2: T): T {
    return t1.letAnyfied { t2 }
}

inline fun <@Anyfied T, @Anyfied R> T.letAnyfied(block: (T) -> R): R = block(this)

value class vClass(val v: Int)

// FILE: foo.kt

fun box() {
    val v1 = vClass(1)
    val v2 = vClass(2)
    val second = test(v1, v2)
}

// @FooKt.class:
// 0 ALOAD
// 0 ARETURN
// 0 ASTORE
// 0 valueOf