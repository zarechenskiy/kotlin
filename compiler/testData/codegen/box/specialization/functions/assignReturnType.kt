// WITH_RUNTIME

inline fun <@Anyfied T> returnI(el: T): T {
    return el
}

fun box(): String {
    val arr = returnI(1 as vInt)
    return "OK"
}

value class vInt(val v: Int)