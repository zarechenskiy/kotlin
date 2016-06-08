// WITH_RUNTIME

inline fun <@Anyfied T> takeArray(arr: Array<T>): T {
    return arr[0]
}

fun box(): String {
    val k = intArrayOf(1) as Array<vInt>
    takeArray(k)
    return "OK"
}

// 1 IASTORE
// 0 ILOAD
// 2 ASTORE
// 0 AASTORE
// 1 AALOAD
// 1 IALOAD
// 1 ARETURN