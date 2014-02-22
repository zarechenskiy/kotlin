fun foo() {
    val b: Boolean
    if (1 < 2) {
        b = false
    }
    else {
        b = true
    }
    use(b)
}

fun use(vararg a: Any?) = a
