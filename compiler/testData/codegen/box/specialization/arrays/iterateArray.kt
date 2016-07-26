// WITH_RUNTIME

fun box(): String {
    val v = intArrayOf(1) as Array<vInt>
    for (i in v) {
        val p: vInt = i
    }

    return "OK"
}

value class vInt(val v: Int)