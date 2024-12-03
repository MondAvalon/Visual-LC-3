package parser

import tokenizer.Token

/**
 * 原指令数据类
 */
data class RawInstruction(
    val labels: Set<String>,
    val operator: String,
    val operands: List<Token>
) {
    /**
     * 将此原指令转换为可打印形式。
     */
    fun toFormatted(): String {
        val labelStr = if (labels.isNotEmpty()) "[${labels.joinToString()}]" else ""
        return listOf(labelStr, "<$operator>", operands.joinToString { it.toFormatted() }).joinToString(" ")
    }
}