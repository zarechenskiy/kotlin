// WITH_RUNTIME

inline fun <@Anyfied T> takeLambda(el: T, l: (T) -> Unit) {
    l(el)
}

fun box(): String {
    takeLambda(1 as vInt) {

    }

    return "OK"
}

// 0 CHECKCAST
// 0 intValue
// 3 ALOAD
// 2 ISTORE
// 1 ILOAD
// 0 ASTORE