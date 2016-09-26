// WITH_RUNTIME

inline fun <@Anyfied T> test(el: T): T {
    return el
}

fun box(): String {
    val k: vInt? = vInt(1)

    if (k is vInt) {
        test(k)
    }

    return "OK"
}

value class vInt(val v: Int)