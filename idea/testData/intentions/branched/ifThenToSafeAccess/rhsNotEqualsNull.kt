fun main(args: Array<String>) {
    val foo: String? = "foo"
    if (null != foo<caret>)
        foo.length()
    else
        null
}
