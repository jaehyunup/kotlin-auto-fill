package io.autofill.kotlin.kotlinautofill

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtValueArgumentList

class RandomAutofillQuickFix(
    private val injectConstructorOrFunctionDefaultValue: Boolean
) : LocalQuickFix {
    companion object {
        const val NAME = "Add arguments with Auto-fill (Random)"
    }

    override fun getName() = NAME

    override fun getFamilyName() = name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement as? KtValueArgumentList ?: return
        val parameters = element.getKtDescriptor()?.valueParameters ?: return
        AutofillDelegator.fillArguments(element, parameters, injectConstructorOrFunctionDefaultValue, true)
    }
}
