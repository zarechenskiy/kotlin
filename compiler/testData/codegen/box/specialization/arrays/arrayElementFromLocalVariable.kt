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

// 1 IASTORE
// 1 ILOAD
// 3 ASTORE
// 0 AASTORE
// 1 AALOAD
// 1 IALOAD
// 1 ARETURN
// 2 ISTORE