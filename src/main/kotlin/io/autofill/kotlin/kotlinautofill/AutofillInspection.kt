package io.autofill.kotlin.kotlinautofill;

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.valueArgumentListVisitor
import javax.swing.JComponent

class AutofillInspection(
    @JvmField var visibleMissingArgumentsQuickFix: Boolean = true,
    @JvmField var visibleRandomModeInspectionQuickFix: Boolean = true
) : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        valueArgumentListVisitor { valueArgumentList ->
            val descriptor = valueArgumentList.getKtDescriptor() ?: return@valueArgumentListVisitor
            if (descriptor.valueParameters.size == valueArgumentList.arguments.size) return@valueArgumentListVisitor
            if (visibleMissingArgumentsQuickFix) {
                holder.registerProblem(
                    valueArgumentList,
                    "Add all missing argument names",
                    AddAllArgumentsNameQuickFix()

                )
            }

            holder.registerProblem(
                valueArgumentList,
                "Auto-fill <Default>",
                DefaultAutofillQuickFix()
            )

            if (visibleRandomModeInspectionQuickFix) {
                holder.registerProblem(
                    valueArgumentList,
                    "Auto-Fill <Random>",
                    RandomAutofillQuickFix()
                )
            }
        }

    override fun createOptionsPanel(): JComponent =
        MultipleCheckboxOptionsPanel(this).apply {
            addCheckbox("Visible Random mode Auto-Fill QuickFix", "visibleRandomModeInspectionQuickFix")
            addCheckbox("Visible '${AddAllArgumentsNameQuickFix.NAME}' Auto-Fill QuickFix", "visibleMissingArgumentsQuickFix")
        }
}
