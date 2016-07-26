// WITH_RUNTIME

fun box(): String {
    val arr = intArrayOf(1, 2) as Array<vInt>

    val anyResult1 = arr.anyfiedAny { true }
    val anyResult2 = arr.anyfiedAny { false }

    assert(anyResult1 == true)
    assert(anyResult2 == false)

    return "OK"
}

inline fun <@Anyfied T> Array<out T>.anyfiedAny(predicate: (T) -> Boolean): Boolean {
    for (element in this) if (predicate(element)) return true
    return false
}

value class vInt(val v: Int)