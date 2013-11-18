fun Function1<Int, Int>.noInlineExt(p: Int) {}

fun Function1<Int, Int>.inlineExt2(p: Int) {

}

fun inlineExt2() {
    {(i: Int) -> 1}.inlineExt2(1) //if add ; no error
    {(i: Int) -> 2}
}

fun main(args: Array<String>) {
    inlineExt2 ()
}

fun bar(vararg a: Any) {}

class TestBar {
    val x = bar()
    {}
}


fun foo() {}
fun foo(i: Int) {}
class C {
    val f = foo()

    {

    }
}
