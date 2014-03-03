fun main(args: Array<String>) {
    val foo: String? = "foo"
    val bar = "bar"
    if (foo != null<caret>) {
        foo
    }
    else {
        bar
    }
}
