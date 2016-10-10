// WITH_RUNTIME

value class VLong(val l: Long) {
    operator fun box(): Long {
        return l
    }

    operator fun unbox(boxed: Long): VLong {
        return VLong(boxed)
    }

    fun inc(): VLong = VLong(l + 1)
}

fun takeAny(a: Any): String {
    return "OK"
}

fun box(): String {
    val v = VLong(123)
    return takeAny(v)
}