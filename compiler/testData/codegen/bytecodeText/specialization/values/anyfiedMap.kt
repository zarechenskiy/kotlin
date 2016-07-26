// WITH_RUNTIME

// FILE: library.kt

value class Meters(val m: Int)
value class Miles(val k: Int)

fun testLambda(tarr: Array<vInt>, t: vInt): Array<vInt> {
    return tarr.anyfiedMap(t) { t }
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

// FILE: foo.kt

fun test(meters: Array<Meters>, seed: Miles, newMiles: Miles) {
    val miles = meters.anyfiedMap(seed) { newMiles }
}

// @FooKt.class:
// 0 ANEWARRAY
// 0 CHECKCAST \[Ljava/lang/Object;
// 0 valueOf
// 1 public final static test\(\[III\)V
// 1 NEWARRAY T_INT
