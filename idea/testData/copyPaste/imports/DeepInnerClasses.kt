package a

import a.Outer.Nested.*
import a.Outer.Inner.*

class Outer {
    class Nested {
        class NN {
        }
        class NN2 {
        }
        inner class NI {
        }
        inner class NI2 {
        }
    }

    inner class Inner {
        class IN {
        }
        class IN2 {
        }
        inner class II {
        }
        inner class II2 {
        }
    }
}

fun <T> g(v: T, body: T.() -> Unit) = v.body()

<selection>fun f(p1: NN, p2: NI, p3: IN, p4: II) {
    g (Outer.Nested()) {
        NN2()
        NI2()
    }
    g (Outer().Inner()) {
        IN2()
        II2()
    }
}</selection>