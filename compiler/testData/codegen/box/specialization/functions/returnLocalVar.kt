// WITH_RUNTIME

inline fun <@Anyfied T> returnLocalVar(element: T): T {
    val b = element
    return b
}

fun box(): String {
    returnLocalVar(1 as vInt)
    return "OK"
}

value class vInt(val v: Int)