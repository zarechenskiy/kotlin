// INTENTION_TEXT: Replace with '%' operator
fun test() {
    class Test {
        fun mod(a: Int): Test = Test()
    }
    val test = Test()
    test.mod<caret>(1)
}
