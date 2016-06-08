// WITH_RUNTIME

inline fun <@Anyfied T> returnI(el: T): T {
    return el
}

fun box(): String {
    val arr = returnI(1 as vInt)
    return "OK"
}

// 1 ALOAD
// 1 ILOAD
// 2 ISTORE
// 0 valueOf
// 0 intValue