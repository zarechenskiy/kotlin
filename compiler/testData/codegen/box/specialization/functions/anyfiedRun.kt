// WITH_RUNTIME

fun box(): String {
    test(1 as vInt)
    return "OK"
}

inline fun <@Anyfied T> test(el: T): T {
    return runAnyfied {
        el
    }
}

inline fun <@Anyfied R> runAnyfied(block: () -> R): R = block()
