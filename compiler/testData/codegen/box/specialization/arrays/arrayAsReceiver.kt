// WITH_RUNTIME

inline fun <@Anyfied T> Array<T>.takeArray(): T {
    return this[0]
}

fun box(): String {
    val k = intArrayOf(1) as Array<vInt>
    val p = k.takeArray()
    return "OK"
}

// 1 IASTORE
// 0 ILOAD
// 3 ASTORE
// 0 AASTORE
// 1 AALOAD
// 1 IALOAD
// 1 ARETURN
// 1 ISTORE