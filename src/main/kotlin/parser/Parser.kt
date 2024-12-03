package parser

import tokenizer.Token
import tokenizer.TokenType

/**
 * 由操作码到其操作数类型的映射表。
 *
 * R 寄存器
 * I 立即数
 * A 寄存器或立即数
 * N 数字
 * L 标签
 * S 字符串
 *
 * 空白字符串代表无需参数。
 */
private val argTypes = mapOf(
    ".ORIG" to "I",
    ".FILL" to "I",
    ".BLKW" to "N",
    ".STRINGZ" to "S",
    ".END" to "",

    "GETC" to "",
    "IN" to "",
    "PUTS" to "",
    "OUT" to "",
    "PUTSP" to "",
    "HALT" to "",

    "ADD" to "RRA",
    "AND" to "RRA",

    "BR" to "L",
    "BRN" to "L",
    "BRZ" to "L",
    "BRP" to "L",
    "BRNZ" to "L",
    "BRZP" to "L",
    "BRNP" to "L",
    "BRNZP" to "L",

    // TODO: 请为以下操作码填写参数类型
    "JMP" to "",
    "JSR" to "",
    "JSRR" to "",
    "LD" to "",
    "LDI" to "",
    "LDR" to "",
    "LEA" to "",
    "NOT" to "",
    "RET" to "",
    "RTI" to "",
    "ST" to "",
    "STI" to "",
    "STR" to "",
    "TRAP" to "",
)

/**
 * 将符号表整合成原指令，采用自顶向下、基于类型的分析方法。
 */
fun createRawInstructions(tokens: Iterable<Token>): List<RawInstruction> {
    val instructions = mutableListOf<RawInstruction>()
    val tit = tokens.iterator()

    // 标签 out@ 允许我们稍后使用 break 打破两层循环
    out@ while (tit.hasNext()) {
        val labels = mutableSetOf<String>()

        lateinit var t: Token

        // 匹配标签
        while (true) {
            // 使用 break@out 跳出本循环和外层循环
            if (!tit.hasNext()) break@out
            t = tit.next()
            if (t.type != TokenType.LABEL) break // 读到的不是标签，离开本层循环
            labels.add(t.content)
        }

        // 匹配操作码
        if (t.type != TokenType.OPERATOR) throw IllegalStateException("未知的符号：$t")

        val op = t.content.uppercase()
        val type = argTypes.getOrElse(op) { throw IllegalStateException("未知的操作码：$op") }
        val operands = mutableListOf<Token>()

        // 根据类型信息匹配操作数
        for (o in type) {
            tit.next().let {
                val accepted = when (o) {
                    'R' -> it.type == TokenType.REGISTER
                    'I' -> it.type == TokenType.IMMEDIATE
                    'A' -> it.type == TokenType.REGISTER || it.type == TokenType.IMMEDIATE
                    'N' -> it.type == TokenType.NUMBER
                    'L' -> it.type == TokenType.LABEL
                    'S' -> it.type == TokenType.STRING
                    else -> throw IllegalArgumentException("未知的类型标识符：$o")
                }

                if (!accepted) throw IllegalStateException("符号 $o 与所需操作数类型 $it 不匹配")
                operands.add(it)
            }
        }

        // 将各类 TRAP 指令的别名翻译到 TRAP <IMM> 的格式
        val trapAlias = mapOf(
            "GETC" to "x20",
            "OUT" to "x21",
            "PUTS" to "x22",
            "IN" to "x23",
            "PUTSP" to "x24",
            "HALT" to "x25",
        )

        // 注意这里组合 ?.let 和 ?: 的用法，?.let 后跟 Lambda 表达式在值不为空时执行，?: 后跟的表达式在值为空时执行
        // 这样只需查找一遍 trapAlias[op]
        trapAlias[op]?.let {
            instructions.add(RawInstruction(labels, "TRAP", listOf(Token(TokenType.IMMEDIATE, it))))
        } ?: instructions.add(RawInstruction(labels, op, operands))
    }

    return instructions
}
