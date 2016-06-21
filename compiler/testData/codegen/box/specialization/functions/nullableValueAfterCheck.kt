// WITH_RUNTIME

inline fun <@Anyfied T> test(el: T): T {
    return el
}

fun box(): String {
    val k = 1 as vInt?

    if (k is vInt) {
        test(k)
    }

    return "OK"
}

// 0 ILOAD
// 0 ISTORE
// 3 ALOAD
// 1 valueOf
// 1 intValue