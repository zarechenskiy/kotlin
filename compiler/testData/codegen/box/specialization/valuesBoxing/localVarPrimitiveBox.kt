// WITH_RUNTIME

value class VLong(val l: Long) {
    operator fun box(): Any {
        return java.lang.Long.valueOf(l)
    }

    operator fun unbox(boxed: Any): VLong {
        return VLong(boxed as Long)
    }

    fun inc(): VLong = VLong(l + 1)
}

fun box(): String {
    val v = VLong(123)
    val ls = listOf(v)
    val t = ls[0]

    val g = t.inc()

    return if (g == VLong(124)) "OK" else "Not ok"
}