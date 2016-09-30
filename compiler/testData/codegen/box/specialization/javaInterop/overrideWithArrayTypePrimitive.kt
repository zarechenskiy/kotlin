// WITH_RUNTIME

// FILE: foo.kt

value class VLong(val v: Long)

interface SomeK {
    fun sample(ls: Array<VLong>): Array<VLong>
}

// FILE: A.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class A implements SomeK {
    @Override
    public long[] sample(@NotNull long[] ls) {
        return ls;
    }
}

// FILE: 1.kt

fun box(): String {
    val ls = anyfiedArrayOf(VLong(1), VLong(2))

    val result = A().sample(ls)
    assert(result[1].v == 2L)

    return "OK"
}

inline fun <reified @Anyfied T> anyfiedArrayOf(vararg elements: T): Array<T> {
    return elements as Array<T>
}