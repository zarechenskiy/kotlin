fun main(args: Array<String>) {
    val bar = "bar"
    val foo: String? = "foo"
    if (null == foo<caret>)
        bar
    else
        foo
}
