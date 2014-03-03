package test.collections

import kotlin.test.*
import java.util.*
import org.junit.Test

class SetTest {
    val data = hashSetOf("foo", "bar")

    Test fun any() {
        assertTrue {
            data.any{it.startsWith("f")}
        }
        assertNot {
            data.any{it.startsWith("x")}
        }
    }

    Test fun all() {
        assertTrue {
            data.all{it.length == 3}
        }
        assertNot {
            data.all{(s: String) -> s.startsWith("b")}
        }
    }

    Test fun filter() {
        val foo = data.filter{it.startsWith("f")}.toSet()

        assertTrue {
            foo.all{it.startsWith("f")}
        }
        assertEquals(1, foo.size)

        assertEquals(hashSetOf("foo"), foo)

        assertTrue("Filter on a Set should return a Set") {
            foo is Set<String>
        }
    }

    Test fun find() {
        val x = data.firstOrNull{it.startsWith("x")}
        assertNull(x)

        val f = data.first{it.startsWith("f")}
        assertEquals("foo", f)
    }

    Test fun map() {
        val lengths = data.map{s -> s.length}
        assertTrue {
            lengths.all{it == 3}
        }
        assertEquals(2, lengths.size)
        assertEquals(arrayListOf(3, 3), lengths)
    }

}
