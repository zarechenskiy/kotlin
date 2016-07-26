// WITH_RUNTIME

inline fun <@Anyfied T> returnLocalVar(element: T): T {
    return element
}

fun call(): String {
    returnLocalVar(1 as vInt)
    return "OK"
}

value class vInt(val v: Int)