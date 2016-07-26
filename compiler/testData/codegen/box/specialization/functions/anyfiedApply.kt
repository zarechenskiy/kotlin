// WITH_RUNTIME

fun box(): String {
    val el = 1 as vInt
    val result = test(el)

    return if (result == el) "OK" else "Fail: $result not equals expected ($el)"
}

inline fun <@Anyfied T> test(el: T): T {
    return el.applyAnyfied {  }
}

public inline fun <@Anyfied T> T.applyAnyfied(block: T.() -> Unit): T {
    block()
    return this
}

value class vInt(val v: Int)