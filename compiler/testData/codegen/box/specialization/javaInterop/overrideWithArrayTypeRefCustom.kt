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
    fun sample(ls: Array<NameCustomBox>): Array<NameCustomBox>
}

// FILE: A.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class A implements SomeK {
    @Override
    public String[] sample(@NotNull String[] name) {
        return name;
    }
}

// FILE: 1.kt

fun box(): String {
    val names = anyfiedArrayOf(NameCustomBox("Kotlin"), NameCustomBox("Java"))

    val result = A().sample(names)
    assert(result[0].s == "Kotlin")

    return "OK"
}

inline fun <reified @Anyfied T> anyfiedArrayOf(vararg elements: T): Array<T> {
    return elements as Array<T>
}