// WITH_RUNTIME

// FILE: foo.kt

value class Name(val s: String) {
    operator fun box(): String = s
    operator fun unbox(boxed: String): Name = Name(boxed)
}

interface SomeK {
    fun sample(ls: List<Name>): Int
}

// FILE: SampleWithString.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

interface SampleWithString {
    int sample(@NotNull List<String> ls);
}

// FILE: A.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class A implements SomeK, SampleWithString {
    @Override
    public int sample(@NotNull List<String> ls) {
        return 100;
    }
}

// FILE: 1.kt

fun box(): String {
    val a = A()
    val lsOfName = listOf(Name("Kotlin"))

    val result1 = a.sample(lsOfName)
    assert(result1 == 100)

    val lsOfString = listOf("Kotlin")
    val result2 = (a as SampleWithString).sample(lsOfString)
    assert(result2 == 100)

    return "OK"
}