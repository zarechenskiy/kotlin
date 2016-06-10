// WITH_RUNTIME

fun box(): String {
    val v = intArrayOf(1) as Array<vInt>
    for (i in v) {
        val p: vInt = i
    }

    return "OK"
}

// 0 CHECKCAST
// 0 intValue