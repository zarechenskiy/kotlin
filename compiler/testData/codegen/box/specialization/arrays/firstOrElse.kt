// WITH_RUNTIME

fun box(): String {
    val arr = intArrayOf(1, 2) as Array<vInt>
    val default = vInt(10)

    val f = arr.firstOrElse(default) { false }

    assert(f == default)

    return "OK"
}

inline fun <@Anyfied T> Array<out T>.firstOrElse(default: T, predicate: (T) -> Boolean): T {
    for (element in this) if (predicate(element)) return element
    return default
}

value class vInt(val v: Int)