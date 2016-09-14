// WITH_RUNTIME

// FILE: library.kt

value class VLong(val l: Long) {
    fun box(): Any {
        return java.lang.Long.valueOf(l)
    }

    fun unbox(boxed: Any): VLong {
        return VLong(boxed as Long)
    }

    fun inc(): VLong = VLong(l + 1)
}

fun <T> takeT(a: T) {

}

// FILE: foo.kt

fun box() {
    val v = VLong(123)
    takeT(v)
}

// @FooKt.class:
// 1 INVOKESTATIC VLong\$AnyfiedImpls.box \(J\)Ljava/lang/Object;
// 0 INVOKESPECIAL
// 0 INVOKESTATIC java/lang/Long.valueOf \(J\)Ljava/lang/Long;
// 0 INVOKEVIRTUAL java/lang/Number.longValue \(\)J