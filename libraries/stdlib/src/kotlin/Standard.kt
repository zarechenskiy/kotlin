package kotlin

import java.util.ArrayList
import java.util.HashSet
import java.util.LinkedList

/**
Run function f
*/
public inline fun <T> run(f: () -> T) : T = f()

/**
 * Execute f with given receiver
 */
public inline fun <T, R> with(receiver: T, f: T.() -> R) : R = receiver.f()

/**
 * Converts receiver to body parameter
*/
public inline fun <T:Any, R> T.let(f: (T) -> R): R = f(this)
