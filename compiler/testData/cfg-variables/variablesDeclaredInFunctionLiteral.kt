fun foo() {
    val a = 1
    "before"
    val f = { (x: Int) ->
        val y = x + a
        y
    }
    "after"
}