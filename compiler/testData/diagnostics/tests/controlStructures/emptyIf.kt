fun foo(x: Unit) = x

fun test() {
    foo({if (1==1);}())

    //return if (true);
}
