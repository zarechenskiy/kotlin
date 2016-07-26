// WITH_RUNTIME

inline fun <@Anyfied T> T.testExt(el: T): T {
    return if (3 + 2 > 6) this else el
}

fun box(): String {
    test(1 as vInt, 2 as vInt)
    return "OK"
}

fun test(v1: vInt, v2: vInt): vInt {
    val v3 = v1.testExt(v2)
    return v3
}

value class vInt(val v: Int)