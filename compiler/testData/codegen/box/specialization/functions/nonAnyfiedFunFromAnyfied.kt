// WITH_RUNTIME

inline fun <@Anyfied T> first(el: T): T {
    val k = el
    val f = nonAnyfied(k)
    return f
}

inline fun <R> nonAnyfied(el: R): R {
    return el
}

fun box(): String {
    val v = 1 as vInt
    val p = first(v)

    assert(v == p)

    return "OK"
}
