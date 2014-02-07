package kotlin

public fun <T> countTo(n: Int): (T) -> Boolean {
  var count = 0
  return { ++count; count <= n }
}
