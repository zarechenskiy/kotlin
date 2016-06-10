// WITH_RUNTIME

fun box(): String {
    test(1 as vInt)
    return "OK"
}

inline fun <@Anyfied T> test(el: T): T {
    return el.letAnyfied {
        el
    }
}

public inline fun <@Anyfied T, @Anyfied R> T.letAnyfied(block: (T) -> R): R = block(this)