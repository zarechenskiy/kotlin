// WITH_RUNTIME

inline fun <@Anyfied T> takeVarargs1(vararg elements: T) {
}

fun box(v1: vInt, v2: vInt): String {
    takeVarargs1(v1, v2)
    return "OK"
}

// 0 AALOAD
// 0 AASTORE
// 1 T_INT
// 2 IASTORE
// 0 ISTORE
// 2 ILOAD