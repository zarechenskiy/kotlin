package test.collections

import kotlin.test.assertEquals
import org.junit.Test as test
import kotlin.test.fails
import java.util.ArrayList
import java.util.Vector


class IteratorsTest {

   fun testEnumeration() {
        val v = Vector<Int>()
        for(i in 1..5)
            v.add(i)

        var sum = 0
        for(k in v.elements())
            sum += k

        assertEquals(15, sum)
    }

    test fun iterationOverIterator() {
        val c = arrayListOf(0, 1, 2, 3, 4, 5)
        var s = ""
        for (i in c.iterator()) {
            s = s + i.toString()
        }
        assertEquals("012345", s)
    }
}
