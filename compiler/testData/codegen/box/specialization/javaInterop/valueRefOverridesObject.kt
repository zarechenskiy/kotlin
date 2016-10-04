// WITH_RUNTIME

// FILE: foo.kt

class BoxedGreeter(val s: String)

value class NameCustomBox(val s: String) {
    operator fun box(): BoxedGreeter = BoxedGreeter(s)
    operator fun unbox(boxed: BoxedGreeter): NameCustomBox = NameCustomBox(boxed.s)
}

interface A<T> {
    fun foo(): T
}

open class B : A<NameCustomBox> {
    override fun foo(): NameCustomBox = NameCustomBox("Kotlin")
}

// FILE: ExtendsB.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

class ExtendsB extends B {
    void test() {
        BoxedGreeter x = foo();
        Object z = foo();
    }
}

// FILE: 1.kt

fun box(): String {
    val fooB = B().foo()
    val extendsB = ExtendsB()
    val fooEB: NameCustomBox = extendsB.foo()

    extendsB.test()

    assert(fooB.s == "Kotlin")
    assert(fooEB.s == "Kotlin")

    return "OK"
}