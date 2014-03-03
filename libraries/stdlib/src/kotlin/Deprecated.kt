package kotlin

deprecated("Use firstOrNull function instead.") public inline fun <T> Array<out T>.find(predicate: (T) -> Boolean) : T? = firstOrNull(predicate)
deprecated("Use firstOrNull function instead.") public inline fun <T> Iterable<T>.find(predicate: (T) -> Boolean) : T? = firstOrNull(predicate)
