// WITH_RUNTIME

fun box(): String {
    val k = test(vInt(1)) {
        vInt(2)
    }
    return "OK"
}

inline fun test(el: vInt, init: (vInt) -> vInt): vInt {
    return init(el)
}

value class vInt(val v: Int)