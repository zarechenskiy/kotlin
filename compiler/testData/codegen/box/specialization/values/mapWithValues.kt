// WITH_RUNTIME

value class Meter(val v: Int)
value class Kilometer(val v: Int)

fun box(): String {
    val kilometers = anyfiedArrayOf(1.km, 2.km, 3.km)
    val seed = 0.meter

    val meters = kilometers.anyfiedMap(seed, Kilometer::toMeter)

    assert(kilometers[0].toMeter() == meters[0])
    assert(kilometers[1].toMeter() == meters[1])
    assert(kilometers[2].toMeter() == meters[2])

    assert(meters[2] == 3000.meter)

    return "OK"
}

inline fun <@Anyfied reified T, @Anyfied reified R> Array<out T>.anyfiedMap(z: R, transform: (T) -> R): Array<R> {
    return mapToAnyfied(AnyfiedArray(size) { z }, transform)
}

inline fun <@Anyfied T, @Anyfied R> Array<out T>.mapToAnyfied(destination: Array<R>, transform: (T) -> R): Array<R> {
    for (item in this.getIndices())
        destination[item] = transform(this[item])

    return destination
}

// library functions

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

fun Kilometer.toMeter(): Meter {
    val k = this as Int * 1000
    return k.meter
}

val Int.meter: Meter
    get() = this as Meter

val Int.km: Kilometer
    get() = this as Kilometer