package kotlin

public inline fun <T> List<T>.forEachWithIndex(operation : (Int, T) -> Unit) {
    for (index in indices) {
        operation(index, get(index))
    }
}

