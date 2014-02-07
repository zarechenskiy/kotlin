package kotlin

import kotlin.support.*
import java.util.Collections
import kotlin.test.assertTrue


/** An [[Iterator]] which invokes a function to calculate the next value in the iteration until the function returns *null* */
class FunctionIterator<T:Any>(val nextFunction: () -> T?): AbstractIterator<T>() {
    override protected fun computeNext(): Unit {
        val next = (nextFunction)()
        if (next == null) {
            done()
        } else {
            setNext(next)
        }
    }
}

/** An [[Iterator]] which iterates over a number of iterators in sequence */
fun CompositeIterator<T>(vararg iterators: Iterator<T>): CompositeIterator<T> = CompositeIterator(iterators.iterator())

class CompositeIterator<T>(val iterators: Iterator<Iterator<T>>): AbstractIterator<T>() {

    var currentIter: Iterator<T>? = null

    override protected fun computeNext(): Unit {
        while (true) {
            if (currentIter == null) {
                if (iterators.hasNext()) {
                    currentIter = iterators.next()
                } else {
                    done()
                    return
                }
            }
            val iter = currentIter
            if (iter != null) {
                if (iter.hasNext()) {
                    setNext(iter.next())
                    return
                } else {
                    currentIter = null
                }
            }
        }
    }
}

/** A singleton [[Iterator]] which invokes once over a value */
class SingleIterator<T>(val value: T): AbstractIterator<T>() {
    var first = true

    override protected fun computeNext(): Unit {
        if (first) {
            first = false
            setNext(value)
        } else {
            done()
        }
    }
}

class IndexIterator<T>(val iterator : Iterator<T>): Iterator<Pair<Int, T>> {
    private var index : Int = 0

    override fun next(): Pair<Int, T> {
        return Pair(index++, iterator.next())
    }

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }
}

public class PairIterator<T, S>(
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

class SkippingIterator<T>(val iterator: Iterator<T>, val n: Int): Iterator<T> {
    private var firstTime: Boolean = true

    private fun skip() {
        for (i in 1..n) {
            if (!iterator.hasNext()) break
            iterator.next()
        }
        firstTime = false
    }

    override fun next(): T {
        assertTrue(!firstTime, "hasNext() must be invoked before advancing an iterator")
        return iterator.next()
    }

    override fun hasNext(): Boolean {
        if (firstTime) {
            skip()
        }
        return iterator.hasNext()
    }
}

