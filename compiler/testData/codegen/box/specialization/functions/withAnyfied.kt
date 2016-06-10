// WITH_RUNTIME

fun box(): String {
    test(1 as vInt)
    return "OK"
}

inline fun <@Anyfied T> test(el: T): T {
    return withAnyfied(el) {
        el
    }
}

public inline fun <@Anyfied T, @Anyfied R> withAnyfied(receiver: T, block: T.() -> R): R = receiver.block()