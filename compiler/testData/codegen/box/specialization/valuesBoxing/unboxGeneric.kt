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

fun <T> takeAndReturnT(a: T): T {
    return a
}

fun box(): String {
    val v = VLong(123)
    val c = takeAndReturnT(v)

    return if (v == c) "OK" else "Not equals"
}