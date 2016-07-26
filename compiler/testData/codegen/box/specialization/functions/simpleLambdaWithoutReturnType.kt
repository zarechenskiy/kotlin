// WITH_RUNTIME

inline fun <@Anyfied T> takeLambda(el: T, l: (T) -> Unit) {
    l(el)
}

fun box(): String {
    takeLambda(1 as vInt) {

    }

    return "OK"
}

value class vInt(val v: Int)