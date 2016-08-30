// WITH_RUNTIME

value class VLong(val v: Long) {
    fun <T : Number> createMin(el: T): VLong {
        val l = el.toLong()
        return if (v < l) this else VLong(l)
    }

    fun inc(): VLong = VLong(v + 1)

    fun add(l: Long): VLong = VLong(v + l)

    fun sqr(): VLong = this * this

    fun lessThan(other: VLong): Boolean = this < other

    operator fun times(other: VLong): VLong = VLong(this.v * other.v)

    operator fun compareTo(other: VLong): Int {
        return v.compareTo(other.v)
    }

    fun factorial(): VLong {
        return VLong(factorial(v, 1))
    }

    private fun factorial(n: Long, acc: Long): Long {
        return if (n == 0L)
            acc
        else
            factorial(n - 1, n * acc)
    }
}

fun box(): String {
    val v = VLong(2)

    val a = v.add(10) // 12

    val b = v.inc().inc() // 4

    val c = b.sqr() // 16

    val less = b.lessThan(c)

    val f = b.factorial()

    val e1 = v.createMin(1)
    val e2 = v.createMin(3)

    assert(a.v == 12L)
    assert(b.v == 4L)
    assert(c.v == 16L)
    assert(less)
    assert(f.v == 24L)
    assert(e1.v == 1L)
    assert(e2.v == 2L)

    return "OK"
}