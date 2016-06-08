// WITH_RUNTIME

fun box(v1: vInt, v2: vInt): String {
    val p = takeVarargs1(v1, v2)
    return "OK"
}

inline fun <@Anyfied T> takeVarargs1(vararg elements: T): T {
    return elements[0]
}

// 1 AALOAD
// 0 AASTORE
// 1 T_INT
// 2 IASTORE
// 1 ISTORE
// 2 ILOAD
// 0 valueOf
// 0 intValue