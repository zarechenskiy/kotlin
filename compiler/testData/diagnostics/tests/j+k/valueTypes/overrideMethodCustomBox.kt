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

fun box(
        a: A,
        lsOfName: List<NameCustomBox>,
        lsOfBoxedGreeters: List<BoxedGreeter>,
        lsOfGreeters: List<Greeter>,
        lsOfString: List<String>) {
    a.sample(lsOfName)
    a.sample(<!TYPE_MISMATCH!>lsOfBoxedGreeters<!>)
    a.sample(<!TYPE_MISMATCH!>lsOfGreeters<!>)
    a.sample(<!TYPE_MISMATCH!>lsOfString<!>)
}