// WITH_RUNTIME

inline fun <@Anyfied T> returnI(el: T): T {
    return el
}

fun test(): vInt {
    return returnI(1 as vInt)
}

fun box(): String {
    test()
    return "OK"
}

value class vInt(val v: Int)