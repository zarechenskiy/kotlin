// WITH_RUNTIME

inline fun <@Anyfied T> takeArray(arr: Array<T>): T {
    return arr.get(0)
}

fun box(): String {
    val k = intArrayOf(1) as Array<vInt>
    val p = takeArray(k)
    return "OK"
}

value class vInt(val v: Int)