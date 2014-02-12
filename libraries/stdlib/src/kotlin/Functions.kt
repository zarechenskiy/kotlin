package kotlin

public fun <T: Any> Function1<T, T?>.toGenerator(initialValue: T): Function0<T?> {
    var nextValue: T? = initialValue
    return {
        nextValue?.let { result ->
            nextValue = this@toGenerator(result)
            result
        }
    }
}

public fun <T> countTo(n: Int): (T) -> Boolean {
    var count = 0
    return { ++count; count <= n }
}

public inline fun <T> List<T>.forEachWithIndex(operation : (Int, T) -> Unit) {
    for (index in indices) {
        operation(index, get(index))
    }
}
