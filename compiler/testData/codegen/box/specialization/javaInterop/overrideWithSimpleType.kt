// WITH_RUNTIME

// FILE: foo.kt

value class Name(val s: String) {
    operator fun box(): String = s
    operator fun unbox(boxed: String): Name = Name(boxed)
}

interface SomeK {
    fun sample(ls: Name): Name
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
    val result = A().sample(Name("Kotlin"))
    assert(result.s == "Kotlin")

    return "OK"
}