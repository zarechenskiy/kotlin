// WITH_RUNTIME

inline fun <@Anyfied T> takeArray(arr: Array<T>) {

}

fun box(): String {
    val k = intArrayOf(1) as Array<vInt>
    takeArray(k)
    return "OK"
}

// 1 IASTORE
// 0 IALOAD
// 2 ASTORE
// 0 AASTORE
// 0 AALOAD