package io.autofill.kotlin.kotlinautofill.thirdparty

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.resolveToCall
import org.jetbrains.kotlin.load.java.descriptors.JavaCallableMemberDescriptor
import org.jetbrains.kotlin.psi.KtCallElement
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

fun KtElement.getKtDescriptor(): CallableDescriptor? {
    val descriptor =
        this.getStrictParentOfType<KtCallElement>()?.calleeExpression?.resolveToCall()?.resultingDescriptor

    // enable only Kotlin
    if (descriptor is JavaCallableMemberDescriptor) return null

    return descriptor?.takeIf { ktDescriptor ->
        ktDescriptor is ClassConstructorDescriptor || ktDescriptor is SimpleFunctionDescriptor
    }
}
