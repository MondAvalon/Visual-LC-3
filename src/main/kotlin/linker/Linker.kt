package linker

import parser.RawInstruction

/**
 * 记录标签与其地址之间的对应关系。
 */
class Linker {
    private val symbols = mutableMapOf<String, Int>() // Key 为标签名，Value 为地址

    /**
     * 向符号表中加入一个标签。
     */
    fun putLabel(label: String, addr: Int) {
        symbols.compute(label) { k, v ->
            v?.let { throw IllegalStateException("标签已存在：$k") } ?: addr
        }
    }

    /**
     * 查询标签并获取其地址。
     */
    fun getLabelAddress(label: String): Int =
        symbols.getOrElse(label) { throw IllegalArgumentException("没有名为 $label 的标签") }
}

/**
 * 循环访问各条指令，并计算标签的地址。
 */
fun createLinker(ins: Iterable<RawInstruction>): Linker {
    var pc: Int? = null
    val linker = Linker()
    for (i in ins) {
        when (i.operator) {
            ".ORIG" -> pc = i.operands.first().asImmediate() // 把 PC 设置为 .ORIG 后指定的地址
            ".END" -> pc = null
            else -> {
                if (pc == null) throw IllegalStateException("指令不在可寻址范围内")
                if (pc > 0xffff || pc < 0x0) throw IllegalStateException("无法对 $pc 编址")

                i.labels.forEach {
                    // 对每个标签，将其加入 linker 所记录的符号表中
                    // TODO("请实现此方法的剩余部分")
                    linker.putLabel(it, pc)
                }

                when (i.operator) {
                    ".STRINGZ" -> pc += i.operands.first().asStringContent().length + 1
                    ".BLKW" -> pc += i.operands.first().asNumber().coerceAtLeast(0)
                    else -> pc++
                }
            }
        }
    }

    return linker
}