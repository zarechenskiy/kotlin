// WITH_RUNTIME

inline fun <@Anyfied T, @Anyfied R> takeLambda(el: T, l: (T) -> R): R {
    return l(el)
}

fun box(): String {
    takeLambda(1 as vInt) {
        1 as vInt
    }

    return "OK"
}

// 0 CHECKCAST
// 0 intValue
// 3 ALOAD
// 2 ISTORE
// 1 ILOAD
// 0 ASTORE