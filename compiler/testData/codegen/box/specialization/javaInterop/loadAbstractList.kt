// WITH_RUNTIME

import java.util.*

private class MyReversedList<T>(private val delegate: MutableList<T>) : AbstractMutableList<T>() {
    override val size: Int get() = delegate.size
    override fun get(index: Int): T = TODO()

    override fun clear() = delegate.clear()
    override fun removeAt(index: Int): T = TODO()

    override fun set(index: Int, element: T): T = TODO()
    override fun add(index: Int, element: T) {
        TODO()
    }
}

fun box(): String {
    val c = MyReversedList<Int>(ArrayList<Int>())

    assert(c.size == 0)

    return "OK"
}