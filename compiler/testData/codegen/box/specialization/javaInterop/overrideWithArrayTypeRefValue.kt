// WITH_RUNTIME

// FILE: foo.kt

value class Name(val s: String) {
    operator fun box(): String = s
    operator fun unbox(boxed: String): Name = Name(boxed)
}

interface SomeK {
    fun sample(ls: Array<Name>): Array<Name>
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
    val names = anyfiedArrayOf(Name("Kotlin"), Name("Java"))

    val result = A().sample(names)
    assert(result[0].s == "Kotlin")

    return "OK"
}

inline fun <reified @Anyfied T> anyfiedArrayOf(vararg elements: T): Array<T> {
    return elements as Array<T>
}