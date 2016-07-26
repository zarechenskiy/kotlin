// WITH_RUNTIME

fun box(): String {
    val k = test(1 as vInt) {
        2 as vInt
    }
    return "OK"
}

inline fun <@Anyfied T> test(el: vInt, init: (vInt) -> T): T {
    return init(el)
}

value class vInt(val v: Int)