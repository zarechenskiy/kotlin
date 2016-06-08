// WITH_RUNTIME

inline fun <@Anyfied T> aloadAstore(t: T) {
    val t1 = t
}

fun box(): String {
    aloadAstore(1 as vInt)
    return "OK"
}

// 1 ALOAD
// 1 ILOAD
// 2 ISTORE
// 1 ASTORE
// 0 valueof
// 0 intValue