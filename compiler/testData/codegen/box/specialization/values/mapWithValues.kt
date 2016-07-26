// WITH_RUNTIME

value class Meters(val m: Int)
value class Miles(val k: Int)

fun box(): String {
    val meters = intArrayOf(1, 2, 3) as Array<Meters>
    val seed = 0 as Miles
    val newMile = 42 as Miles

    val miles = meters.anyfiedMap(seed) { newMile }

    assert(miles[0] == newMile)
    assert(miles[1] == newMile)
    assert(miles[2] == newMile)

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