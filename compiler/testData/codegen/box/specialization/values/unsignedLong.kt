// WITH_RUNTIME

fun box(): String {
    val uLongs = anyfiedArrayOf(uLong(1), uLong(2), uLong(3), uLong(4))

    val squares = uLongs.anyfiedMap(uLong(0)) { it }

    for (u in uLongs) {
        assert(u.value() == u.value() * u.value())
    }

    return "OK"
}

value class uLong(private val v: Long)

val uLong.MIN_VALUE: uLong
    get() = uLong(0)

val uLong.MAX_VALUE: uLong
    get() = uLong(-1L)

// arithmetic operations

operator fun uLong.plus(other: uLong): uLong {
    return uLong(this.value() + other.value())
}

operator fun uLong.minus(other: uLong): uLong {
    return uLong(this.value() - other.value())
}

operator fun uLong.times(other: uLong): uLong {
    return uLong(this.value() * other.value())
}

// ..

// implements Comparable<uInt>
operator fun uLong.compareTo(other: uLong): Int {
    return java.lang.Long.compare(this.flip(), other.flip()) // or use compareUnsigned from JDK8
}

// implements Number interface
fun uLong.toByte(): Byte = unsigned().toByte()
fun uLong.toShort(): Short = unsigned().toShort()
fun uLong.toLong(): Long = value()
fun uLong.toFloat(): Float = unsigned().toFloat()
fun uLong.toDouble(): Double = unsigned().toDouble()
fun uLong.toChar(): Char = unsigned().toChar()

// --

private val uLong.UNSIGNED_MASK: Long
    get() = 0x7fffffffffffffffL

private fun uLong.value(): Long = this as Long

private fun uLong.flip(): Long = value() xor Long.MIN_VALUE

private fun uLong.unsigned(): Long = value() and UNSIGNED_MASK



// utility functions

inline fun <@Anyfied reified T, @Anyfied reified R> Array<out T>.anyfiedMap(z: R, transform: (T) -> R): Array<R> {
    return mapToAnyfied(AnyfiedArray(size) { z }, transform)
}

inline fun <@Anyfied T, @Anyfied R> Array<out T>.mapToAnyfied(destination: Array<R>, transform: (T) -> R): Array<R> {
    for (item in this.getIndices())
        destination[item] = transform(this[item])

    return destination
}

inline fun <reified @Anyfied T> AnyfiedArray(size: Int, init: (Int) -> T): Array<T> {
    val result = createAnyfiedArray<T>(size)
    for (i in 0..size - 1) {
        result[i] = init(i)
    }
    return result
}

inline fun <reified @Anyfied T> createAnyfiedArray(size: Int): Array<T> {
    return arrayOfNulls<T>(size) as Array<T>
}

inline fun <@Anyfied T> Array<out T>.getIndices(): IntRange {
    val end = getLastIndex()
    return IntRange(0, end)
}

inline fun <@Anyfied T> Array<out T>.getLastIndex(): Int {
    return size - 1
}

inline fun <reified @Anyfied T> anyfiedArrayOf(vararg elements: T): Array<T> {
    return elements as Array<T>
}