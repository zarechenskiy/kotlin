fun bar(): String = "bar"

fun main(args: Array<String>) {
    val foo: String? = "foo"
    if (null != foo<caret>)
        foo
    else
        bar()
}
