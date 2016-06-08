// WITH_RUNTIME

inline fun <@Anyfied T> returnLocalVar(element: T): T {
    return element
}

fun call(): String {
    returnLocalVar(1 as vInt)
    return "OK"
}

// 1 ALOAD
// 0 ASTORE
// 1 ILOAD
// 1 ISTORE
// 0 ASTORE
// 0 valueof
// 0 intValue