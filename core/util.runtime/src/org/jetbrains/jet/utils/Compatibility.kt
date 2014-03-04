package org.jetbrains.jet.utils

import java.util.ArrayList
import kotlin.support.AbstractIterator

//TODO: Remove after updating to new stdlib
fun <T: Any> Iterable<T>.firstOrNull() : T? {
    val iterator = this.iterator()
    return if (iterator.hasNext()) iterator.next() else null
}

public fun <T, S> Iterator<T>.zip_tmp(iterator: Iterator<S>): Iterator<Pair<T, S>> = PairIterator_tmp(this, iterator)
public fun <T, S> Iterable<T>.zip_tmp(second: Iterable<S>): Iterable<Pair<T, S>> {
    val list = ArrayList<Pair<T,S>>()
    for (item in iterator().zip_tmp(second.iterator())) {
        list.add(item)
    }
    return list
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

