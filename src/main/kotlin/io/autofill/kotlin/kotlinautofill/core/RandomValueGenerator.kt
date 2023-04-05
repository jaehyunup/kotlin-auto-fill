package io.autofill.kotlin.kotlinautofill.core

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.types.KotlinType
import java.text.DecimalFormat
import java.util.UUID
import kotlin.random.Random


object RandomValueGenerator {
    private val UUID_PATTERN = Regex("""^.*(uuid|Uuid|uUid|UUid|UUID).*""")
    private var intValueRange: IntRange = IntRange(0, 9999)
    private var longValueRange: LongRange = LongRange(0, 9999)
    private var charValuePool: List<Char> = (('A'..'Z') + ('a'..'z') + ('0'..'9'))
    private var decimalFormat: DecimalFormat = DecimalFormat(createDecimalFormatString(3, 3))
    private var floatFormat: DecimalFormat = DecimalFormat("${createDecimalFormatString(3, 3)}f")
    private val randomWordPool = mutableListOf<String>()

    init {
        charValuePool = (('A'..'Z') + ('a'..'z') + ('0'..'9'))
        javaClass.classLoader.getResourceAsStream("words/plausibleNameList")
            .use { it.bufferedReader().forEachLine { word -> randomWordPool.add(word) } }
    }

    fun boolean(): String = Random.nextBoolean().toString()
    fun char(): String = charValuePool.random().toString()
    fun int(): String = intValueRange.random().toString()
    fun long(): String = "${longValueRange.random()}L"
    fun double(): String = decimalFormat.format(Random.nextDouble(0.00, 99.9999))
    fun float(): String = floatFormat.format(Random.nextDouble())
    fun stringOrUuid(name: String): String = if (UUID_PATTERN.matches(name)) {
        UUID.randomUUID().toString()
    } else {
        string().trim()
    }

    private fun string(): String = randomWordPool.random().trim()

    private fun createDecimalFormatString(integerLength: Int, decimalLength: Int): String {
        var temp = ""
        for (i in 0 until integerLength) {
            temp += "#"
        }
        temp += "."
        for (i in 0 until decimalLength) {
            temp += "0"
        }
        return temp
    }
}