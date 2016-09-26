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

fun <T> takeT(a: T): String {
    return "OK"
}

fun box(): String {
    val v = VLong(123)
    val result = takeT(v)

    return result
}