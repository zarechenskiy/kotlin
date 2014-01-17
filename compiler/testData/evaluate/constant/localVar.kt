package test

class A() {
    fun foo() {
        var a = 1

        // val prop5: null
        val prop5 = a

        // val prop6: null
        val prop6 = a + 1

        fun local() {
            // val prop1: null
            val prop1 = a

            // val prop2: null
            val prop2 = a + 1
        }
    }
}

fun foo() {
    var a = 1

    // val prop7: null
    val prop7 = a

    // val prop8: null
    val prop8 = a + 1
}