package io.autofill.kotlin.kotlinautofill.inspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel
import io.autofill.kotlin.kotlinautofill.quickfix.AddAllArgumentsNameQuickFix
import io.autofill.kotlin.kotlinautofill.quickfix.DefaultAutofillQuickFix
import io.autofill.kotlin.kotlinautofill.quickfix.RandomAutofillQuickFix
import io.autofill.kotlin.kotlinautofill.thirdparty.getKtDescriptor
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.valueArgumentListVisitor
import javax.swing.JComponent

@Deprecated("Use K2Inspection instead. This inspection is for Kotlin 1.x only.")
class K1Inspection(
    @JvmField var groupedSuggestion: Boolean = false,
) : AbstractKotlinInspection(), K2modeSupportInspection {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ) = valueArgumentListVisitor { valueArgumentList ->
        println("Running K1Inspection on ${valueArgumentList.text}")
        val descriptor = valueArgumentList.getKtDescriptor()
        val argumentNames = descriptor?.valueParameters ?: return@valueArgumentListVisitor
        val argumentValues = valueArgumentList.arguments

        val hasMissingArgs = argumentNames.size != argumentValues.size

        val allUnnamed = argumentValues.all { it.getArgumentName() == null }

        val hasSomeNamed = argumentValues.any { it.getArgumentName() != null }
        val hasSomeUnnamed = argumentValues.any { it.getArgumentName() == null }
        val hasMixedNamed = hasSomeNamed && hasSomeUnnamed
        if (hasMixedNamed) return@valueArgumentListVisitor

        if (!hasMissingArgs && !allUnnamed) return@valueArgumentListVisitor

        holder.registerProblem(
            valueArgumentList,
            "Some arguments are missing. You can auto-fill them using default values.",
            DefaultAutofillQuickFix(groupedSuggestion),
        )

        holder.registerProblem(
            valueArgumentList,
            "Some arguments are missing. You can auto-fill them using random values.",
            RandomAutofillQuickFix(groupedSuggestion),
        )

        if (allUnnamed) {
            holder.registerProblem(
                valueArgumentList,
                "Add all arguments using named syntax for better readability and completeness.",
                AddAllArgumentsNameQuickFix(groupedSuggestion),
            )
        }
    }

    override fun createOptionsPanel(): JComponent =
        MultipleCheckboxOptionsPanel(this).apply {
            addCheckbox("Visible Auto-Fill Suggestions by group", "groupedSuggestion")
        }

    override fun supportsK2(): Boolean {
        return try {
            Class.forName("org.jetbrains.kotlin.analysis.api.analyze")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
