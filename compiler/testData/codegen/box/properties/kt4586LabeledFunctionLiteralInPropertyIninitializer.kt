//KT-4586 this@ does not work for builders

fun string(init: StringBuilder.() -> Unit): String{
    val answer = StringBuilder()
    answer.init()
    return answer.toString()
}

fun box(): String {
    val str = string @l{
        append("O")

        val sub = string {
            append("K")
            this@l.append(this)  // does nothing !!
        }
    }
    return str
}