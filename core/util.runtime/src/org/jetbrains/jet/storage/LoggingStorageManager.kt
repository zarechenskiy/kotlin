/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.storage

import java.lang.reflect.Field

public class LoggingStorageManager(
        private val delegate: StorageManager,
        private val out: Appendable,
        private val sanitizeHashes: Boolean = false
) : StorageManager {

    // Creating objects here because we need a reference to it
    private val <T> (() -> T).logged: () -> T
        get() = object : () -> T {
            override fun invoke(): T {
                doLog(this@logged, this) {
                    call ->
                    "val ${call.field.getName()} called from '${call.fieldOwner.sanitize()}'"
                }
                return this@logged()
            }
        }

    // Creating objects here because we need a reference to it
    private val <K, V> ((K) -> V).logged: (K) -> V
        get() = object : (K) -> V {
            override fun invoke(k: K): V {
                doLog(this@logged, this) {
                    call ->
                    "fun ${call.field.getName()}('$k') called from '${call.fieldOwner.sanitize()}'"
                }
                return this@logged(k)
            }
        }

    private fun Any?.sanitize(): String {
        return toString().replaceAll("@[0-9a-zA-Z]+", "@<hash>")
    }

    private fun doLog(lambda: Any, logged: Any, body: (CallData) -> String) {
        val call = computeCallData(lambda, logged)
        val fieldOwner = call.fieldOwner.toString().sanitize()

        out.append(body(call)).append("\n")

        val fieldOwnerClassName = call.fieldOwner.javaClass.getName()
        if (!fieldOwner.contains(fieldOwnerClassName)) {
            out.append("\tof type ${fieldOwnerClassName}\n")
        }

        out.append("\n")
    }

    private class CallData(val fieldOwner: Any, val field: Field, val lambdaCreatedIn: Any?)

    private fun computeCallData(lambda: Any, logged: Any): CallData {
        val jClass = lambda.javaClass

        val outerClass = jClass.getEnclosingClass()
        if (outerClass == null) {
            error("No outer class: $lambda")
        }

        // fields named "this" or "this$0"
        val referenceToOuter = jClass.getAllDeclaredFields().find {
            field ->
            field.getType() == outerClass && field.getName()!!.contains("this")
        }
        if (referenceToOuter == null) error("No referenceToOuter: $lambda")
        referenceToOuter.setAccessible(true)

        val outerInstance = referenceToOuter.get(lambda)
        if (outerInstance == null) error("No outer instance: $lambda")

        val containingField = outerClass.getAllDeclaredFields().find {
            (field): Boolean ->
            field.setAccessible(true)
            val value = field.get(outerInstance)
            if (value == null) return@find false

            val valueClass = value.javaClass

            val functionField = valueClass.getAllDeclaredFields().find { f -> f.getType()?.getName()?.startsWith("jet.Function") ?: false }
            if (functionField == null) return@find false

            functionField.setAccessible(true)
            val functionValue = functionField.get(value)
            functionValue == logged
        }
        if (containingField == null) error("Containing field not found: $lambda")

        val enclosingEntity = jClass.getEnclosingConstructor()
                            ?: jClass.getEnclosingMethod()
                            ?: jClass.getEnclosingClass()

        return CallData(outerInstance, containingField, enclosingEntity)
    }

    private fun Class<*>.getAllDeclaredFields(): List<Field> {
        val result = arrayListOf<Field>()

        var c = this
        while (true) {
            result.addAll(c.getDeclaredFields().toList())
            [suppress("UNCHECKED_CAST")]
            val superClass = (c as Class<Any>).getSuperclass() as Class<Any>?
            if (superClass == null) break
            if (c == superClass) break
            c = superClass
        }

        return result
    }

    override fun createMemoizedFunction<K, V: Any>(compute: (K) -> V): MemoizedFunctionToNotNull<K, V> {
        return delegate.createMemoizedFunction(compute.logged)
    }

    override fun createMemoizedFunctionWithNullableValues<K, V: Any>(compute: (K) -> V?): MemoizedFunctionToNullable<K, V> {
        return delegate.createMemoizedFunctionWithNullableValues(compute.logged)
    }

    override fun createLazyValue<T: Any>(computable: () -> T): NotNullLazyValue<T> {
        return delegate.createLazyValue(computable.logged)
    }

    override fun createRecursionTolerantLazyValue<T: Any>(computable: () -> T, onRecursiveCall: T): NotNullLazyValue<T> {
        return delegate.createRecursionTolerantLazyValue(computable.logged, onRecursiveCall)
    }

    override fun createLazyValueWithPostCompute<T: Any>(computable: () -> T, onRecursiveCall: ((Boolean) -> T)?, postCompute: (T) -> Unit): NotNullLazyValue<T> {
        return delegate.createLazyValueWithPostCompute(computable.logged, onRecursiveCall, postCompute)
    }

    override fun createNullableLazyValue<T: Any>(computable: () -> T?): NullableLazyValue<T> {
        return delegate.createNullableLazyValue(computable.logged)
    }

    override fun createRecursionTolerantNullableLazyValue<T: Any>(computable: () -> T?, onRecursiveCall: T?): NullableLazyValue<T> {
        return delegate.createRecursionTolerantNullableLazyValue(computable.logged, onRecursiveCall)
    }

    override fun createNullableLazyValueWithPostCompute<T: Any>(computable: () -> T?, postCompute: (T?) -> Unit): NullableLazyValue<T> {
        return delegate.createNullableLazyValueWithPostCompute(computable.logged, postCompute)
    }

    override fun compute<T>(computable: () -> T): T {
        return delegate.compute(computable.logged)
    }
}