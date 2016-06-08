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

// 1 ALOAD
// 1 ILOAD
// 1 ISTORE
// 1 ASTORE
// 0 valueof
// 0 intValue