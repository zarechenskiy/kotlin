import test.*

inline fun test1(param: String): String {
    var result = "fail"
    inlineFun("stub2")  { c->
        {
            inlineFun("stub") { a ->
                {

                    {
                        result = param
                    }()
                }()
            }
        }()
    }

    return result
}


inline fun test2(param: String): String {
    var result = "fail"
    inlineFun("stub") { a ->
        {
            {
                result = param
            }()
        }()
    }

    return result
}


fun box(): String {
    if (test1("start1") != "start1") return "fail1: ${test1("start1")}"
    if (test2("start2") != "start2") return "fail2: ${test2("start2")}"

    return "OK"
}