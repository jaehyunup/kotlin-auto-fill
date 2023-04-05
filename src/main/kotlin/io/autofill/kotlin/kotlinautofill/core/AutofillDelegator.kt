package io.autofill.kotlin.kotlinautofill.core

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.idea.core.ShortenReferences
import org.jetbrains.kotlin.idea.imports.importableFqName
import org.jetbrains.kotlin.idea.intentions.callExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.resolve.lazy.descriptors.LazyClassDescriptor
import org.jetbrains.kotlin.types.KotlinType

object AutofillDelegator {
    fun fillArguments(
        ktValueArgumentList: KtValueArgumentList,
        parameters: List<ValueParameterDescriptor>,
        enableDefaultArgument: Boolean = true,
        randomness: Boolean
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

            val added =
                ktValueArgumentList.addArgument(generateArgument(parameter, factory, enableDefaultArgument, randomness))

            val argumentExpression = added.getArgumentExpression()

            if (argumentExpression is KtQualifiedExpression || argumentExpression is KtLambdaExpression) {
                ShortenReferences.DEFAULT.process(argumentExpression)
            }
        }
    }


    private fun generateArgument(
        parameter: ValueParameterDescriptor,
        factory: KtPsiFactory,
        enableDefaultArgument: Boolean,
        randomMode: Boolean
    ): KtValueArgument {
        val type = parameter.type
        if (!enableDefaultArgument) {
            return factory.createArgument(null, parameter.name)
        }
        val injectionValue = if (randomMode) {
            calculateRandomValue(type, parameter.name.toString())
        } else {
            calculateDefaultValue(type)
        }

        if (injectionValue != null) {
            return factory.createArgument(factory.createExpression(injectionValue), parameter.name)
        } else {
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
                        fillArguments(
                            ktValueArgumentList = argumentsList,
                            parameters = valueParameters,
                            randomness = randomMode
                        )
                    }
                }
            } else {
                null
            }

            return factory.createArgument(argumentExpression, parameter.name)
        }
    }

    private fun calculateRandomValue(type: KotlinType, name: String): String? {
        return when {
            KotlinBuiltIns.isString(type) -> "\"${RandomValueGenerator.stringOrUuid(name)}\""
            KotlinBuiltIns.isBoolean(type) -> RandomValueGenerator.boolean()
            KotlinBuiltIns.isChar(type) -> "'${RandomValueGenerator.char()}'"
            KotlinBuiltIns.isDouble(type) -> RandomValueGenerator.double()
            KotlinBuiltIns.isFloat(type) -> RandomValueGenerator.float()
            KotlinBuiltIns.isInt(type) || KotlinBuiltIns.isShort(type) -> RandomValueGenerator.int()
            KotlinBuiltIns.isLong(type) -> RandomValueGenerator.long()
            KotlinBuiltIns.isCollectionOrNullableCollection(type) -> "arrayOf()"
            KotlinBuiltIns.isNullableAny(type) -> "null"
            KotlinBuiltIns.isListOrNullableList(type) -> "listOf()"
            KotlinBuiltIns.isSetOrNullableSet(type) -> "setOf()"
            KotlinBuiltIns.isMapOrNullableMap(type) -> "mapOf()"
            type.isFunctionType -> "{ -> }"
            type.isMarkedNullable -> "null"
            else -> null
        }
    }

    private fun calculateDefaultValue(type: KotlinType): String? {
        return when {
            KotlinBuiltIns.isString(type) -> "\"\""
            KotlinBuiltIns.isBoolean(type) -> "false"
            KotlinBuiltIns.isChar(type) -> "''"
            KotlinBuiltIns.isDouble(type) -> "0.0"
            KotlinBuiltIns.isFloat(type) -> "0.0f"
            KotlinBuiltIns.isInt(type) || KotlinBuiltIns.isShort(type) -> "0"
            KotlinBuiltIns.isLong(type) -> "0L"
            KotlinBuiltIns.isCollectionOrNullableCollection(type) -> "arrayOf()"
            KotlinBuiltIns.isNullableAny(type) -> "null"
            KotlinBuiltIns.isListOrNullableList(type) -> "listOf()"
            KotlinBuiltIns.isSetOrNullableSet(type) -> "setOf()"
            KotlinBuiltIns.isMapOrNullableMap(type) -> "mapOf()"
            type.isFunctionType -> "{ -> }"
            type.isMarkedNullable -> "null"
            else -> null
        }
    }

}