// WITH_RUNTIME

inline fun <@Anyfied T, @Anyfied R> takeLambda(el: T, l: (T) -> R): R {
    return l(el)
}

fun box(): String {
    takeLambda(1 as vInt) {
        1 as vInt
    }

    return "OK"
}

value class vInt(val v: Int)