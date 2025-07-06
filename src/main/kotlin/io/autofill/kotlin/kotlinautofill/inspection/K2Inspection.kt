package io.autofill.kotlin.kotlinautofill.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ui.MultipleCheckboxOptionsPanel
import io.autofill.kotlin.kotlinautofill.quickfix.AddAllArgumentsNameQuickFix
import io.autofill.kotlin.kotlinautofill.quickfix.DefaultAutofillQuickFix
import io.autofill.kotlin.kotlinautofill.quickfix.RandomAutofillQuickFix
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.KtVisitorVoid
import javax.swing.JComponent

class K2Inspection(
    @JvmField var groupedSuggestion: Boolean = false,
) : AbstractKotlinInspection(), K2modeSupportInspection {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): KtVisitorVoid {
        return object : KtVisitorVoid() {
            override fun visitValueArgumentList(argumentList: KtValueArgumentList) {
                analyze(argumentList) {
                    holder.registerProblem(
                        argumentList,
                        "Some arguments are missing. You can auto-fill them using default values.",
                        ProblemHighlightType.INFORMATION,
                        DefaultAutofillQuickFix(groupedSuggestion),
                    )

                    holder.registerProblem(
                        argumentList,
                        "Some arguments are missing. You can auto-fill them using random values.",
                        ProblemHighlightType.INFORMATION,
                        RandomAutofillQuickFix(groupedSuggestion),
                    )

                    holder.registerProblem(
                        argumentList,
                        "Add all arguments using named syntax for better readability and completeness.",
                        ProblemHighlightType.INFORMATION,
                        AddAllArgumentsNameQuickFix(groupedSuggestion),
                    )
                }
            }
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
