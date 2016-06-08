// WITH_RUNTIME

inline fun <@Anyfied T> returnLocalVar(element: T): T {
    val b = element
    return b
}

fun box(): String {
    returnLocalVar(1 as vInt)
    return "OK"
}

// 2 ALOAD
// 1 ASTORE
// 2 ILOAD
// 2 ISTORE
// 1 ASTORE
// 0 valueof
// 0 intValue