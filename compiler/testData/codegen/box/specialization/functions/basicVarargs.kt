// WITH_RUNTIME

inline fun <@Anyfied T> takeVarargs1(vararg elements: T) {
}

fun box(v1: vInt, v2: vInt): String {
    takeVarargs1(v1, v2)
    return "OK"
}

value class vInt(val v: Int)