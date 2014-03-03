fun main(args: Array<String>) {
    val foo: String? = "foo"
    if (foo == null<caret>)
        null
    else
        foo.length()
}
