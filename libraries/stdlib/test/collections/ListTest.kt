package test.collections

import java.util.ArrayList
import kotlin.test.*
import org.junit.Test as test

class ListTest {

    test fun _toString() {
        val data = arrayListOf("foo", "bar")
        assertEquals("[foo, bar]", data.toString())
    }

    test fun emptyHead() {
        val data = ArrayList<String>()
        assertNull(data.head)
    }

    test fun head() {
        val data = arrayListOf("foo", "bar")
        assertEquals("foo", data.head)
    }

    test fun tail() {
        val data = arrayListOf("foo", "bar", "whatnot")
        val actual = data.tail
        val expected = arrayListOf("bar", "whatnot")
        assertEquals(expected, actual)
    }

    test fun emptyFirst() {
        val data  = ArrayList<String>()
        assertNull(data.first)
    }

    test fun first() {
        val data = arrayListOf("foo", "bar")
        assertEquals("foo", data.first)
    }

    test fun last() {
        val data = arrayListOf("foo", "bar")
        assertEquals("bar", data.last)
    }

    test fun forEachWithIndex() {
        val data = arrayListOf("foo", "bar")
        var index = 0

        data.withIndices().forEach {
            assertEquals(it.first, index)
            assertEquals(it.second, data[index])
            index++
        }

        assertEquals(data.size(), index)
    }

    test fun withIndices() {
        val data = arrayListOf("foo", "bar")
        var index = 0

        for ((i, d) in data.withIndices()) {
            assertEquals(i, index)
            assertEquals(d, data[index])
            index++
        }

        assertEquals(data.size(), index)
    }

    test fun lastIndex() {
        val emptyData = ArrayList<String>()
        val data = arrayListOf("foo", "bar")

        assertEquals(-1, emptyData.lastIndex)
        assertEquals(1, data.lastIndex)
    }
}
