// "class com.intellij.codeInsight.daemon.impl.quickfix.ImportClassFixBase" "false"
// ERROR: Unresolved reference: SomeTest
// ACTION: Edit intention settings
// ACTION: Replace safe access expression With 'if' expression
// ACTION: Disable 'Replace Safe Access Expression With 'if' Expression'

package testing

val x = testing?.<caret>SomeTest()
