package io.autofill.kotlin.kotlinautofill

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.idea.base.fe10.codeInsight.newDeclaration.Fe10KotlinNameSuggester
import org.jetbrains.kotlin.idea.core.CollectingNameValidator
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.imports.importableFqName
import org.jetbrains.kotlin.idea.intentions.callExpression
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.resolve.lazy.descriptors.LazyClassDescriptor
import org.jetbrains.kotlin.types.KotlinType
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

object AutofillDelegator {
    private val UUID_PATTERN = Regex("""^.*(uuid|Uuid|uUid|UUid|UUID).*""")
    const val ZERO_UUID_STRING = "\"00000000-0000-0000-0000-000000000000\""

    fun fillArguments(
        ktValueArgumentList: KtValueArgumentList,
        parameters: List<ValueParameterDescriptor>,
        enableDefaultArgument: Boolean = true,
        randomness: Boolean = false
    ) {
        val arguments = ktValueArgumentList.arguments
        val argumentNames = arguments.mapNotNull { it.getArgumentName()?.asName?.identifier }
        val factory = KtPsiFactory(ktValueArgumentList)

        parameters.forEachIndexed { index, parameter ->
            // ignore when argument count over than parameters count
            // ignore already declared arguments
            if (arguments.size > index && !arguments[index].isNamed()) return@forEachIndexed
            // exclusive parameter if exists argumentNames
            if (parameter.name.identifier in argumentNames) return@forEachIndexed
            if (!enableDefaultArgument && parameter.declaresDefaultValue()) return@forEachIndexed

            val added = ktValueArgumentList.addArgument(generateArgument(parameter, factory, enableDefaultArgument))

            val argumentExpression = added.getArgumentExpression()

            if (argumentExpression is KtQualifiedExpression || argumentExpression is KtLambdaExpression) {
                ShortenReferences.DEFAULT.process(argumentExpression)
            }
        }
    }


    private fun generateArgument(
        parameter: ValueParameterDescriptor,
        factory: KtPsiFactory,
        enableDefaultArgument: Boolean
    ): KtValueArgument {
        val type = parameter.type
        val name = parameter.name
        val clazz = parameter::class

        if (!enableDefaultArgument) {
            return factory.createArgument(null, parameter.name)
        }
        val defaultValue = calculateDefaultValue(type, name, clazz)

        if (defaultValue != null) {
            return factory.createArgument(factory.createExpression(defaultValue), parameter.name)
        }

        val descriptor = type.constructor.declarationDescriptor as? LazyClassDescriptor
        val modality = descriptor?.modality
        if (descriptor?.kind == ClassKind.ENUM_CLASS || modality == Modality.ABSTRACT || modality == Modality.SEALED) {
            return factory.createArgument(null, parameter.name)
        }
        val fqName = descriptor?.importableFqName?.asString()
        val valueParameters =
            descriptor?.constructors?.firstOrNull { it is ClassConstructorDescriptor }?.valueParameters
        val argumentExpression = if (fqName != null && valueParameters != null) {
            (factory.createExpression("$fqName()")).also {
                val callExpression = it as? KtCallExpression ?: (it as? KtQualifiedExpression)?.callExpression
                callExpression?.valueArgumentList?.let { argumentsList ->
                    fillArguments(argumentsList, valueParameters)
                }
            }
        } else {
            null
        }
        return factory.createArgument(argumentExpression, parameter.name)
    }

    private fun calculateDefaultValue(type: KotlinType, name: Name, clazz: KClass<*>): String? {
        return when {
            KotlinBuiltIns.isBoolean(type) -> "false"
            KotlinBuiltIns.isChar(type) -> "''"
            KotlinBuiltIns.isDouble(type) -> "0.0"
            KotlinBuiltIns.isFloat(type) -> "0.0f"
            KotlinBuiltIns.isInt(type) || KotlinBuiltIns.isLong(type) || KotlinBuiltIns.isShort(type) -> "0"
            KotlinBuiltIns.isCollectionOrNullableCollection(type) -> "arrayOf()"
            KotlinBuiltIns.isNullableAny(type) -> "null"
            KotlinBuiltIns.isString(type) -> return if (UUID_PATTERN.matches(name.toString())) {
                "\"00000000-0000-0000-0000-000000000000\""
            } else {
                "\"\""
            }
            KotlinBuiltIns.isListOrNullableList(type) -> "listOf()"
            KotlinBuiltIns.isSetOrNullableSet(type) -> "setOf()"
            KotlinBuiltIns.isMapOrNullableMap(type) -> "mapOf()"
            clazz.jvmName == "UUID" -> "UUID.fromString($ZERO_UUID_STRING)"
            type.isFunctionType -> generateLambdaDefault(type)
            type.isMarkedNullable -> "null"
            else -> null
        }
    }

    private fun generateLambdaDefault(ktType: KotlinType): String =
        buildString {
            append("{")
            if (ktType.arguments.size > 2) {
                val validator = CollectingNameValidator()
                val lambdaParameters = ktType.arguments.dropLast(1).joinToString(postfix = "->") {
                    val type = it.type
                    val name = Fe10KotlinNameSuggester.suggestNamesByType(type, validator, "param")[0]
                    validator.addName(name)
                    val typeText =
                        type.constructor.declarationDescriptor?.importableFqName?.asString() ?: type.toString()
                    val nullable = if (type.isMarkedNullable) "?" else ""
                    "$name: $typeText$nullable"
                }
                append(lambdaParameters)
            }
            append("}")
        }
}