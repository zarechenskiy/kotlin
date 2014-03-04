package org.jetbrains.jet.utils

import java.util.ArrayList
import kotlin.support.AbstractIterator

//TODO: Remove after updating to new stdlib
fun <T: Any> Iterable<T>.firstOrNull_tmp() : T? {
    val iterator = this.iterator()
    return if (iterator.hasNext()) iterator.next() else null
}

public inline fun <K, V> Map<K,V>.filter_tmp(predicate: (Map.Entry<K,V>)->Boolean) : List<Map.Entry<K,V>> {
    return filterTo_tmp(ArrayList<Map.Entry<K,V>>(), predicate)
}

public inline fun <K, V, C: MutableCollection<in Map.Entry<K,V>>> Map<K,V>.filterTo_tmp(collection: C, predicate: (Map.Entry<K,V>) -> Boolean) : C {
    for (element in this) if (predicate(element)) collection.add(element)
    return collection
}

public fun <T, S> Iterator<T>.zip_tmp(iterator: Iterator<S>): Iterator<Pair<T, S>> = PairIterator_tmp(this, iterator)
public fun <T, S> Iterable<T>.zip_tmp(second: Iterable<S>): Iterable<Pair<T, S>> {
    val list = ArrayList<Pair<T,S>>()
    for (item in iterator().zip_tmp(second.iterator())) {
        list.add(item)
    }
    return list
}

public fun <T, R> Iterator<T>.map_tmp(transform : (T) -> R) : Iterator<R> {
    return MapIterator_tmp<T, R>(this, transform)
}

class MapIterator_tmp<T, R>(val iterator : Iterator<T>, val transform: (T) -> R) : AbstractIterator<R>() {
    override protected fun computeNext() : Unit {
        if (iterator.hasNext()) {
            setNext((transform)(iterator.next()))
        } else {
            done()
        }
    }
}

public fun <T> Iterable<T>.withIndices_tmp() : Iterator<Pair<Int, T>> {
    return IndexIterator_tmp(iterator())
}

public fun <T> Array<out T>.withIndices_tmp() : Iterator<Pair<Int, T>> {
    return IndexIterator_tmp(iterator())
}

class IndexIterator_tmp<T>(val iterator : Iterator<T>): Iterator<Pair<Int, T>> {
    private var index : Int = 0

    override fun next(): Pair<Int, T> {
        return Pair(index++, iterator.next())
    }

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }
}


public class PairIterator_tmp<T, S>(
        val iterator1 : Iterator<T>, val iterator2 : Iterator<S>
): AbstractIterator<Pair<T, S>>() {
    protected override fun computeNext() {
        if (iterator1.hasNext() && iterator2.hasNext()) {
            setNext(Pair(iterator1.next(), iterator2.next()))
        }
        else {
            done()
        }
    }
}

