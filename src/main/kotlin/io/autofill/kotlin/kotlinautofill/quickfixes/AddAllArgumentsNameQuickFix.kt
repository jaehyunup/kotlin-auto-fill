package io.autofill.kotlin.kotlinautofill.quickfixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import io.autofill.kotlin.kotlinautofill.core.AutofillDelegator
import io.autofill.kotlin.kotlinautofill.thirdparty.getKtDescriptor
import org.jetbrains.kotlin.psi.KtValueArgumentList

class AddAllArgumentsNameQuickFix : LocalQuickFix {
    companion object {
        const val NAME = "Add missing named call arguments with Auto-Fill"
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
