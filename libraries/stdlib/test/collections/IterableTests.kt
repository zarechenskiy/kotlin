package test.collections

import org.junit.Test
import kotlin.test.*

class SetTest : IterableBaseTests<Set<String>>(setOf("foo", "bar"), setOf<String>())
class ListTest : IterableBaseTests<List<String>>(listOf("foo", "bar"), listOf<String>())

abstract class IterableBaseTests<T : Iterable<String>>(val data: T, val empty: T) {
    Test fun any() {
        expect(true) { data.any { it.startsWith("f") } }
        expect(false) { data.any { it.startsWith("x") } }
        expect(false) { empty.any { it.startsWith("x") } }
    }

    Test fun all() {
        expect(true) { data.all { it.length == 3 } }
        expect(false) { data.all { it.startsWith("b") } }
        expect(true) { empty.all { it.startsWith("b") } }
    }

    Test fun none() {
        expect(false) { data.none { it.length == 3 } }
        expect(false) { data.none { it.startsWith("b") } }
        expect(true) { data.none { it.startsWith("x") } }
        expect(true) { empty.none { it.startsWith("b") } }
    }

    Test fun filter() {
        val foo = data.filter { it.startsWith("f") }
        expect(true) { foo is List<String> }
        expect(true) { foo.all { it.startsWith("f") } }
        expect(1) { foo.size }
        assertEquals(setOf("foo"), foo.toSet())
    }

    Test fun filterNot() {
        val notFoo = data.filterNot { it.startsWith("f") }
        expect(true) { notFoo is List<String> }
        expect(true) { notFoo.none { it.startsWith("f") } }
        expect(1) { notFoo.size }
        assertEquals(setOf("bar"), notFoo.toSet())
    }

    Test fun forEach() {
        var count = 0
        data.forEach { count += it.length }
        assertEquals(6, count)
    }

    Test fun first() {
        expect("foo") { data.first() }
        fails {
            data.first { it.startsWith("x") }
        }
        fails {
            empty.first()
        }
        expect("foo") { data.first { it.startsWith("f") } }
    }

    Test fun firstOrNull() {
        expect(null) { data.firstOrNull { it.startsWith("x") } }
        expect(null) { empty.firstOrNull() }

        val f = data.firstOrNull { it.startsWith("f") }
        assertEquals("foo", f)
    }

    Test fun last() {
        assertEquals("bar", data.last())
        fails {
            data.last { it.startsWith("x") }
        }
        fails {
            empty.last()
        }
        expect("foo") { data.last { it.startsWith("f") } }
    }

    Test fun lastOrNull() {
        expect(null) { data.lastOrNull { it.startsWith("x") } }
        expect(null) { empty.lastOrNull() }
        expect("foo") { data.lastOrNull { it.startsWith("f") } }
    }

    Test fun elementAt() {
        expect("foo") { data.elementAt(0) }
        expect("bar") { data.elementAt(1) }
        fails { data.elementAt(2) }
        fails { data.elementAt(-1) }
        fails { empty.elementAt(0) }

    }

    Test fun indexOf() {
        expect(0) { data.indexOf("foo") }
        expect(-1) { empty.indexOf("foo") }
        expect(1) { data.indexOf("bar") }
        expect(-1) { data.indexOf("zap") }
    }

    Test fun single() {
        fails { data.single() }
        fails { empty.single() }
        expect("foo") { data.single { it.startsWith("f") } }
        expect("bar") { data.single { it.startsWith("b") } }
        fails {
            data.single { it.length == 3 }
        }
    }

    Test
    fun singleOrNull() {
        fails { data.singleOrNull() }
        fails { empty.singleOrNull() }
        expect("foo") { data.singleOrNull { it.startsWith("f") } }
        expect("bar") { data.singleOrNull { it.startsWith("b") } }
        fails {
            data.singleOrNull { it.length == 3 }
        }
    }

    Test
    fun map() {
        val lengths = data.map { it.length }
        assertTrue {
            lengths.all { it == 3 }
        }
        assertEquals(2, lengths.size)
        assertEquals(arrayListOf(3, 3), lengths)
    }

    Test
    fun max() {
        expect("foo") { data.max() }
        expect("bar") { data.maxBy { it.last() } }
    }

    Test
    fun min() {
        expect("bar") { data.min() }
        expect("foo") { data.minBy { it.last() } }
    }

    Test
    fun count() {
        expect(2) { data.count() }
        expect(0) { empty.count() }

        expect(1) { data.count { it.startsWith("f") } }
        expect(0) { empty.count { it.startsWith("f") } }

        expect(0) { data.count { it.startsWith("x") } }
        expect(0) { empty.count { it.startsWith("x") } }
    }

    Test
    fun withIndices() {
        var index = 0
        for ((i, d) in data.withIndices()) {
            assertEquals(i, index)
            assertEquals(d, data.elementAt(index))
            index++
        }
        assertEquals(data.count(), index)
    }

    Test
    fun fold() {

    }

    Test
    fun reduce() {

    }


}
