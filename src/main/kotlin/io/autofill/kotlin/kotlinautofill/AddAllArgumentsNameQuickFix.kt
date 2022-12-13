package io.autofill.kotlin.kotlinautofill

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtValueArgumentList

class AddAllArgumentsNameQuickFix : LocalQuickFix {
    companion object {
        const val NAME = "Add missing named call arguments"
    }

    override fun getName() = NAME

    override fun getFamilyName() = name

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement as? KtValueArgumentList ?: return
        val parameters = element.getKtDescriptor()?.valueParameters ?: return
        AutofillDelegator.fillArguments(
            ktValueArgumentList = element,
            parameters = parameters,
            enableDefaultArgument = false,
            randomness = false
        )
    }
}
