package codegen

import linker.Linker
import parser.RawInstruction
import tokenizer.TokenType

/**
 * 将数字转换为补码形式。
 */
private fun Int.toComplement(bits: Int = 9): Int {
    // 注意 Kotlin 中不支持 << 和 & 等位运算符，需要使用 shl 和 and 等来代替
    val upperLimit = (1 shl (bits - 1)) - 1
    val lowerLimit = -(1 shl (bits - 1))
    val mask = (1 shl bits) - 1

    if (this > upperLimit || this < lowerLimit) throw IllegalArgumentException("无法将 $this 编码为 $bits 位")
    return this and mask
}

/**
 * 依据原指令和链接器提供的符号表，生成机器代码。
 */
class CodeGenerator(private val instructions: Iterable<RawInstruction>, private val linker: Linker) {
    /**
     * 自增后的 PC 值
     */
    private var pci: Int? = null

    /**
     * 计算 PC 到目标地址的偏移量
     */
    private fun getPCOffset(target: String): Int {
        require(pci != null)
        return linker.getLabel(target) - pci!!
    }

    /**
     * 生成机器代码。
     */
    fun build(): List<UShort> {
        // 输出程序列表，机器码以 UShort 即 16 位无符号数存储
        val output = mutableListOf<UShort>()

        for (ins in instructions) {
            // 使用 ins 来引用当前指令
            when (ins.operator) {
                ".ORIG" -> pci = ins.operands.first().asImmediate() + 1
                ".END" -> pci = null
                else -> {
                    if (pci == null) throw IllegalStateException("指令不在可寻址范围内")

                    if (ins.operator.startsWith("BR")) {
                        // BR 指令单独处理
                        val (label) = ins.operands
                        // 注意如何使用 ifEmpty 来实现 BR 等效于 BRNZP 的逻辑
                        val cond = ins.operator.substring(2).ifEmpty { "NZP" }
                        val cc = (if ("N" in cond) 4 else 0) + (if ("Z" in cond) 2 else 0) + (if ("P" in cond) 1 else 0)
                        val out = (cc shl 9) or (getPCOffset(label.asLabel()).toComplement(9))
                        output.add(out.toUShort())
                    } else when (ins.operator) {
                        "ADD", "AND" -> {
                            // 利用解构赋值提取 ins.operands 的前三项元素，并赋值给 dr, sr1, op2
                            val (dr, sr1, op2) = ins.operands
                            val opCode = if (ins.operator == "AND") 0b0101 else 0b0001
                            // 使用 + 或者 or 结合移位，将操作码和操作数组合成指令
                            // shl 12，即将操作码填入 16-12 位
                            // shl 9，即将寄存器编号填入 11-9 位
                            // 注意使用 asRegisterId 获取寄存器编号
                            var out = (opCode shl 12) or (dr.asRegisterId() shl 9) or (sr1.asRegisterId() shl 6)

                            // 判断第二个操作数的类型，并相应处理
                            out = if (op2.type == TokenType.IMMEDIATE) {
                                // 使用 asImmediate 获取立即数数值，然后使用 toComplement 转换为补码
                                out or (1 shl 5) or (op2.asImmediate().toComplement(5))
                            } else {
                                out or (op2.asRegisterId())
                            }

                            // 将生成的数据添加到输出，需要调用 toUShort 转换为 16 位无符号数
                            output.add(out.toUShort())
                        }


                        "JMP" -> {
                            val (dr) = ins.operands
                            // 请继续使用 dr 生成 JMP 的机器代码
                            TODO()
                        }

                        "JSR" -> {
                            val (label) = ins.operands
                            // 注意，在使用 getPCOffset 获取偏移量后，要使用 toComplement 方法将其转换为补码
                            val offset = getPCOffset(label.asLabel()).toComplement(11)
                            // 请继续使用偏移量生成 JSR 的机器代码
                            TODO()
                        }

                        "JSRR" -> {
                            TODO()
                        }

                        "LD" -> {
                            TODO()
                        }

                        "LDI" -> {
                            TODO()
                        }

                        "LDR" -> {
                            TODO()
                        }

                        "LEA" -> {
                            TODO()
                        }

                        "NOT" -> {
                            TODO()
                        }

                        "RET" -> {
                            TODO()
                        }

                        "RTI" -> {
                            TODO()
                        }

                        "ST" -> {
                            TODO()
                        }

                        "STI" -> {
                            TODO()
                        }

                        "STR" -> {
                            TODO()
                        }

                        "TRAP" -> {
                            val (vec) = ins.operands
                            val out = (0b1111 shl 12) or vec.asImmediate().toUByte().toInt()
                            output.add(out.toUShort())
                        }

                        ".STRINGZ" -> {
                            ins.operands.first().asStringContent().forEach {
                                // 添加字符编码
                                output.add(it.code.toUShort())
                            }
                            output.add(0u) // 结尾的 0
                        }

                        ".BLKW" -> output.addAll(List(ins.operands.first().asNumber()) { 0u })

                        ".FILL" -> {
                            TODO()
                        }

                        else -> throw IllegalArgumentException("Unknown operator: ${ins.operator}")
                    }

                    // Updates the PC
                    pci = when (ins.operator) {
                        ".STRINGZ" -> pci!! + ins.operands.first().asStringContent().length + 1
                        ".BLKW" -> pci!! + ins.operands.first().asNumber().coerceAtLeast(0)
                        else -> pci!! + 1
                    }
                }
            }
        }

        return output
    }
}