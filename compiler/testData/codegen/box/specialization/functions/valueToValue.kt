// WITH_RUNTIME

fun box(): String {
    val k = test(1 as vInt) {
        2 as vInt
    }
    return "OK"
}

inline fun test(el: vInt, init: (vInt) -> vInt): vInt {
    return init(el)
}