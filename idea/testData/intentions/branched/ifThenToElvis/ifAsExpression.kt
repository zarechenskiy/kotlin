fun main(args: Array<String>) {
    val foo: String? = "foo"
    val bar = "bar"
    val x = if (foo == null<caret>) {
        bar
    }
    else {
        foo
    }
}
