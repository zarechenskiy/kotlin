// WITH_RUNTIME

fun testLambda(tarr: Array<vInt>, t: vInt) {
    tarr.anyfiedMap(t) { t }
}

inline fun <@Anyfied reified T, @Anyfied reified R> Array<out T>.anyfiedMap(z: R, transform: (T) -> R): Array<R> {
    return mapToAnyfied(Array(size) { z }, transform)
}

inline fun <@Anyfied T, @Anyfied R> Array<out T>.mapToAnyfied(destination: Array<R>, transform: (T) -> R): Array<R> {
    for (item in this.indices)
        destination[item] = transform(this[item])

    return destination
}

fun box(): String {
    testLambda(intArrayOf(1) as Array<vInt>, 1 as vInt)
    return "OK"
}