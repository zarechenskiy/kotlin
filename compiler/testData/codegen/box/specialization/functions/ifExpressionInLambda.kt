// WITH_RUNTIME

fun box(): String {
    test(1 as vInt, 2 as vInt)
    return "OK"
}

inline fun <@Anyfied T> test(el1: T, el2: T): T {
    return runAnyfied {
        if (3 + 2 > 6) el1 else el2
    }
}

inline fun <@Anyfied R> runAnyfied(block: () -> R): R = block()