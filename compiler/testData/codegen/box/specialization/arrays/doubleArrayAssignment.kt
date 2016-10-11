// WITH_RUNTIME

// check that we do not specialize anything in this case

fun box(): String {
    val arr1 = anyfiedArrayOf(VInt(1))
    val arr2 = arrayOf(anyfiedArrayOf(VInt(1)))

    arr1.test1(arr2)
    arr2.test2(arr1)

    return "OK"
}

inline fun <@Anyfied T> Array<T>.test1(destination: Array<Array<T>>) {
    destination[0] = this
}

inline fun <@Anyfied T> Array<Array<T>>.test2(destination: Array<T>) {
    this[0] = destination
}

value class VInt(val v: Int) {
    operator fun box(): Int = v
    operator fun unbox(boxed: Int) = VInt(boxed)
}

inline fun <reified @Anyfied T> anyfiedArrayOf(vararg elements: T): Array<T> {
    return elements as Array<T>
}