package io.autofill.kotlin.kotlinautofill.inspections;

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel
import io.autofill.kotlin.kotlinautofill.quickfixes.AddAllArgumentsNameQuickFix
import io.autofill.kotlin.kotlinautofill.quickfixes.DefaultAutofillQuickFix
import io.autofill.kotlin.kotlinautofill.quickfixes.RandomAutofillQuickFix
import io.autofill.kotlin.kotlinautofill.thirdparty.getKtDescriptor
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.valueArgumentListVisitor
import javax.swing.JComponent

class DefaultInspection(
    @JvmField var visibleMissingArgumentsQuickFix: Boolean = true,
    @JvmField var visibleRandomModeInspectionQuickFix: Boolean = true
    // TODO : please changed [codeinsight.api.classic.inspections.AbstractKotlinInspection]
) : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        valueArgumentListVisitor { valueArgumentList ->
            val descriptor = valueArgumentList.getKtDescriptor() ?: return@valueArgumentListVisitor
            if (descriptor.valueParameters.size == valueArgumentList.arguments.size) return@valueArgumentListVisitor
            holder.registerProblem(
                valueArgumentList,
                "Auto-fill <Default>",
                DefaultAutofillQuickFix()
            )

            if (visibleMissingArgumentsQuickFix) {
                holder.registerProblem(
                    valueArgumentList,
                    "Add all missing argument names",
                    AddAllArgumentsNameQuickFix()
                )
            }

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
