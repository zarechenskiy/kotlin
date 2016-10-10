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
    fun sample(ls: NameCustomBox): NameCustomBox
}

// FILE: A.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class A implements SomeK {
    @Override
    public String sample(@NotNull String name) {
        return name;
    }
}

// FILE: 1.kt

fun box(): String {
    val result = A().sample(NameCustomBox("Kotlin"))
    assert(result.s == "Kotlin")

    return "OK"
}