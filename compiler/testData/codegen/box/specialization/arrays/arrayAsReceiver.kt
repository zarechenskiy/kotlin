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

// 1 IASTORE
// 0 ILOAD
// 3 ASTORE
// 0 AASTORE
// 1 AALOAD
// 1 IALOAD
// 1 ARETURN
// 1 ISTORE