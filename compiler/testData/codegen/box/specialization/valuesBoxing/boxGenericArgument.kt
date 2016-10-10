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

fun <T> takeT(a: T): String {
    return "OK"
}

fun box(): String {
    val v = VLong(123)
    val result = takeT(v)

    return result
}