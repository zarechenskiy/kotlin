// WITH_RUNTIME

// FILE: foo.kt

interface Greeter {
    fun greeting(): String
}

value class NameCustomBox(val s: String) : Greeter {
    operator fun box(): BoxedGreeter = BoxedGreeter(s)
    operator fun unbox(boxed: BoxedGreeter): NameCustomBox = NameCustomBox(boxed.s)
    override fun greeting(): String = ""
}

class BoxedGreeter(val s: String) : Greeter {
    override fun greeting(): String = ""
}

interface SomeK {
    fun sample(ls: List<NameCustomBox>): Int
}

// FILE: A.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class A implements SomeK {
    @Override
    public int sample(@NotNull List<BoxedGreeter> ls) {
        return 100;
    }
}

// FILE: 1.kt

fun box(): String {
    val name = NameCustomBox("Kotlin")

    val a = A()
    val lsOfName = listOf(name)

    val result = a.sample(lsOfName)
    assert(result == 100)

    return "OK"
}