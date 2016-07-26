// WITH_RUNTIME

inline fun <@Anyfied T> test1(t: T) {
    val t1 = t
}

fun callTest1(v: vInt) {
    test1(v)
}

fun box(): String {
    callTest1(1 as vInt)
    return "OK"
}

value class vInt(val v: Int)