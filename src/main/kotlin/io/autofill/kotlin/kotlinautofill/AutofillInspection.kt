package io.autofill.kotlin.kotlinautofill;

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.valueArgumentListVisitor

class AutofillInspection(
    @JvmField var injectConstructorOrFunctionDefaultValue: Boolean = true,
    @JvmField var enableRandomValue: Boolean = false
) : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        valueArgumentListVisitor { valueArgumentList ->
            val descriptor = valueArgumentList.getKtDescriptor() ?: return@valueArgumentListVisitor
            if (descriptor.valueParameters.size == valueArgumentList.arguments.size) return@valueArgumentListVisitor

            addDefaultCheckBox(
                this,
                listOf(
                    CheckBoxProperty("Use declared default arguments", "injectConstructorOrFunctionDefaultValue"),
                    CheckBoxProperty("Use random values to fill arguments", "enableRandomValue")
                )
            )

            holder.registerProblem(
                valueArgumentList,
                "Add arguments auto-fill",
                DefaultAutofillQuickFix(injectConstructorOrFunctionDefaultValue)
            )

            if(enableRandomValue){
                holder.registerProblem(
                    valueArgumentList,
                    "Add arguments auto-fill",
                    RandomAutofillQuickFix(injectConstructorOrFunctionDefaultValue)
                )

            }

        }

    private fun addDefaultCheckBox(owner: InspectionProfileEntry, properties: List<CheckBoxProperty>) {
        MultipleCheckboxOptionsPanel(owner).apply {
            properties.forEach {
                this.addCheckbox(it.label, it.property)
            }
        }
    }
}
