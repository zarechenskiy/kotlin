class B {
    companion object <error>A</error> {
    }

    val <error>A</error> = this
}

class C {
    companion <error>object A</error> {
        <error>val A</error> = this
    }

}

class D {
    companion <error>object A</error> {
        <error>val `OBJECT$`</error> = this
    }

    val `OBJECT$` = D
}