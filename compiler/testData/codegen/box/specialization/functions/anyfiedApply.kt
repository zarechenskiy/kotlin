// WITH_RUNTIME

fun box(): String {
    test(1 as vInt)
    return "OK"
}

inline fun <@Anyfied T> test(el: T): T {
    return el.applyAnyfied {  }
}

public inline fun <@Anyfied T> T.applyAnyfied(block: T.() -> Unit): T { block(); return this }