// WITH_RUNTIME

value class uInt(private val v: Int)

operator fun uInt.compareTo(other: uInt): Int {
    return Integer.compare(this.flip(), other.flip())
}

operator fun uInt.plus(other: uInt): uInt {
    return uInt(this.value() + other.value())
}

operator fun uInt.minus(other: uInt): uInt {
    return uInt(this.value() - other.value())
}

fun box(): String {
    val minusOne = uInt(-1)
    val zero = uInt(0)
    val minusTen = uInt(-10)
    val ten = uInt(10)
    val otherTen = uInt(10)

    assert(minusOne > zero)
    assert(minusOne > minusTen)
    assert(ten > zero)
    assert(ten == otherTen)
    assert(minusOne + uInt(1) == zero)
    assert(zero + ten == ten)

    return "OK"
}

private fun uInt.value(): Int {
    return this as Int
}

private fun uInt.flip(): Int {
    return value() xor Int.MIN_VALUE
}