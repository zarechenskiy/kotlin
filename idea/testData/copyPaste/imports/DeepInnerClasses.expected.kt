package to

import a.Outer.Nested.NN
import a.Outer.Nested.NI
import a.Outer.Inner.IN
import a.Outer.Inner.II
import a.g
import a.Outer
import a.Outer.Nested.NN2
import a.Outer.Inner.IN2

fun f(p1: NN, p2: NI, p3: IN, p4: II) {
    g (Outer.Nested()) {
        NN2()
        NI2()
    }
    g (Outer().Inner()) {
        IN2()
        II2()
    }
}