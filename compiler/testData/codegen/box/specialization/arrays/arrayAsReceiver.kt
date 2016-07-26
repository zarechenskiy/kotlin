// WITH_RUNTIME

inline fun <@Anyfied T> Array<T>.takeArray(): T {
    return this[0]
}

fun box(): String {
    val el = 1
    val k = intArrayOf(el) as Array<vInt>
    val p = k.takeArray()

    return if (p == (el as vInt)) "OK" else "Fail: $p not equals $el"
}

value class vInt(val v: Int)