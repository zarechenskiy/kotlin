// WITH_RUNTIME

// check that we do not specialize anything in this case

fun box(): String {
    val arr1 = intArrayOf(1) as Array<vInt>
    val arr2 = arrayOf(intArrayOf(1) as Array<vInt>)

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