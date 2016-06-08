// WITH_RUNTIME

fun box(): String {
    anyfiedArrayOf(1 as vInt, 2 as vInt)
    return "OK"
}

inline fun <reified @Anyfied T> anyfiedArrayOf(vararg elements: T): Array<out T> {
    return elements
}

// 0 AALOAD
// 0 AASTORE
// 1 T_INT
// 1 IASTORE
// 0 ISTORE
// 1 ILOAD
// 0 valueOf
// 0 intValue