package io.autofill.kotlin.kotlinautofill.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import io.autofill.kotlin.kotlinautofill.core.MissingArgumentFiller
import io.autofill.kotlin.kotlinautofill.thirdparty.getKtDescriptor
import org.jetbrains.kotlin.psi.KtValueArgumentList

class AddAllArgumentsNameQuickFix(
    val groupedSuggestion: Boolean,
) : LocalQuickFix {
    companion object {
        const val NAME = "AutoFill: fill missing arguments(only syntax names)"
        const val FAMILY_NAME = "AutoFill"
    }

    override fun getName() = NAME

    override fun getFamilyName(): String {
        return if (groupedSuggestion) FAMILY_NAME else NAME
    }

    override fun applyFix(
        project: Project,
        descriptor: ProblemDescriptor,
    ) {
        val element = descriptor.psiElement as? KtValueArgumentList ?: return

        MissingArgumentFiller.fillArguments(
            ktValueArgumentListElement = element,
            parameterDescriptors = element.getKtDescriptor()?.valueParameters ?: return,
            enableDefaultArgument = false,
            randomness = false,
            onlyFillNames = true,
        )
    }
}
