// WITH_RUNTIME

inline fun <@Anyfied T> takeLambda(el: T, l: (T) -> T) {
    l(el)
}

fun box(): String {
    takeLambda(1 as vInt) {
        1 as vInt
    }

    return "OK"
}

value class vInt(val v: Int)