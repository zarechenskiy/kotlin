// WITH_RUNTIME

inline fun <@Anyfied T> takeArray(arr: Array<T>): T {
    val k = arr.get(0)
    return k
}

fun box(): String {
    val k = intArrayOf(1) as Array<vInt>
    val p = takeArray(k)
    return "OK"
}

value class vInt(val v: Int)