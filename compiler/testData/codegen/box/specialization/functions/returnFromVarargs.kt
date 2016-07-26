// WITH_RUNTIME

fun box(v1: vInt, v2: vInt): String {
    val p = takeVarargs1(v1, v2)
    return "OK"
}

inline fun <@Anyfied T> takeVarargs1(vararg elements: T): T {
    return elements[0]
}

value class vInt(val v: Int)