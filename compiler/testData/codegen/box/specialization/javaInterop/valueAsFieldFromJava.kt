// WITH_RUNTIME


// FILE: foo.kt

value class VInt(val v: Int) {
    operator fun box(): Int = v
    operator fun unbox(boxed: Int) = VInt(boxed)

    fun inc(): VInt = VInt(v + 1)
}

class Normal(val first: VInt) {
    companion object {
        @JvmField val COMP = VInt(3)
        @JvmField val DEFAULTS = listOf(VInt(1), VInt(2))
    }

    var second = VInt(2)

    fun bar(): Boolean {
        second = second.inc()
        val b1 = first.v < second.v
        val b2 = second.v == COMP.v

        return b1 && b2
    }
}

// FILE: Bar.java

import java.util.*;

class Bar {
    void foo() {
        Normal n = new Normal(10);

        List<Integer> ds = Normal.DEFAULTS;
        int c = Normal.COMP;

        n.setSecond(1);

        boolean b = n.bar();

        if (b) throw new RuntimeException("b should be false");

        if (n.getFirst() != 10) throw new RuntimeException();
    }
}

// FILE: baz.kt

fun box(): String {
    val c = Bar()
    c.foo()

    return "OK"
}