// WITH_RUNTIME

// FILE: library.kt

value class VLong(val l: Long) {
    operator fun box(): Any {
        return java.lang.Long.valueOf(l)
    }

    operator fun unbox(boxed: Any): VLong {
        return VLong(boxed as Long)
    }

    fun inc(): VLong = VLong(l + 1)
}

// FILE: foo.kt

fun box() {
    val v = VLong(123)
    val ls = listOf(v)
    val t = ls[0]

    val g = t.inc()
}

// @FooKt.class:
// 1 INVOKESTATIC VLong\$AnyfiedImpls.box \(J\)Ljava/lang/Object;
// 1 INVOKESTATIC VLong\$AnyfiedImpls.unbox \(Ljava/lang/Object;\)J
// 1 INVOKESTATIC VLong\$AnyfiedImpls.inc \(J\)J
// 0 INVOKESPECIAL
// 0 INVOKESTATIC java/lang/Long.valueOf \(J\)Ljava/lang/Long;
// 0 INVOKEVIRTUAL java/lang/Number.longValue \(\)J