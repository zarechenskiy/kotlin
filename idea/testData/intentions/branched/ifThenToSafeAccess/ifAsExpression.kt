fun main(args: Array<String>) {
    val foo: String? = null
    val x = if (foo == null<caret>) {
        null
    }
    else {
        foo.length
    }
}
