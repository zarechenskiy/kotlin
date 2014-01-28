package test

inline fun <R> mfun(f: () -> R) {
    f()
    f()
}
