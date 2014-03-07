/*
 * Copyright 2010-2014 JetBrains s.r.o.
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

package org.jetbrains.jet.plugin.framework

import com.intellij.ide.util.frameworkSupport.FrameworkSupportProvider
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ui.configuration.FacetsProvider
import javax.swing.Icon
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel
import com.intellij.ide.util.frameworkSupport.FrameworkSupportConfigurable
import org.jetbrains.jet.plugin.JetIcons
import javax.swing.JComponent
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.libraries.Library


public class KotlinFrameworkSupportProvider: FrameworkSupportProvider("Kotlin", "Kotlin") {
    override fun createConfigurable(model: FrameworkSupportModel): FrameworkSupportConfigurable {
        return KotlinFrameworkSupportConfigurable()
    }

    override fun getIcon(): Icon? = JetIcons.SMALL_LOGO

    override fun isSupportAlreadyAdded(module: Module, facetsProvider: FacetsProvider): Boolean {
        return false
    }

    override fun isEnabledForModuleType(moduleType: ModuleType<out ModuleBuilder?>): Boolean {
        return true
    }
}

class KotlinFrameworkSupportConfigurable: FrameworkSupportConfigurable() {
    override fun getComponent(): JComponent? = null
    override fun addSupport(module: Module, model: ModifiableRootModel, library: Library?) {
    }
}