package tokenizer

/**
 * 词法分析器类，用于分析输入字符串并将其转换为符号。
 */
class Tokenizer(private val source: CharSequence) {
    private var index = 0
    private val tokens = mutableListOf<Token>()
    private val buffer = StringBuilder()

    /**
     * 词法分析器入口方法，解析源代码并创建符号串。
     */
    fun resolve(): List<Token> {
        while (true) {
            // 若游标已在源代码末尾，则停止循环
            if (index == source.length) break
            val pi = index

            // 尝试读取每种类型的符号
            // 如果读取后游标向前移动，则成功读入，跳转至下一次循环
            // 否则报告错误

            // 注意如何使用 Lambda 函数、find 方法以及 ?: 运算符来简化实现
            // 若有一个步骤执行完后 pi != index，则 find 方法返回非 null 值，循环由此继续
            // 否则 find 方法在尝试所有的符号后返回 null，经由 ?: 运算符执行异常退出
            listOf(
                { readString() },
                { readWhitespaces() },
                { readComment() },
                { readWord() }
            ).find {
                it()
                pi != index
            } ?: throw IllegalArgumentException("无法识别的符号：${peekNext()}")
        }

        return tokens
    }

    /**
     * 查看输入字符串的下一个字符，但不移进游标。
     */
    private fun peekNext(): Char = source[index]

    /**
     * 从输入字符串中读取下一个字符，并将其加入缓冲区，随后移进游标。
     */
    private fun pushNext(): Char = source[index++].also { buffer.append(it) }

    /**
     * 如果缓冲区非空，则按给定的符号类型创建一个符号，将其加入符号串。
     * 否则不做任何工作。
     *
     * 缓冲区将在使用后清空。
     */
    private fun addToken(type: TokenType) {
        if (buffer.isNotEmpty()) {
            Token(type, buffer.toString()).let {
                buffer.clear()
                tokens.add(it)
            }
        }
    }


    /**
     * 尝试读取一个带引号的字符串。
     */
    private fun readString() {
        if (peekNext() != '"') return

        pushNext() // 将引号加入缓冲区

        var isEscape = false
        while (index < source.length) {
            val c = peekNext()
            if (isEscape) {
                isEscape = false
                index++ // 丢弃掉当前字符，不将其加入缓冲区
                buffer.append(
                    when (c) {
                        'n' -> '\n'
                        't' -> '\t'
                        'b' -> '\b'
                        else -> c // 反斜线和引号的情况也在这条分支中处理
                    }
                )
            } else {
                if (c == '\\') {
                    // 遇到反斜线，标记转义
                    isEscape = true
                    index++
                } else {
                    // 将下一个字符作为字符串的一部分加入缓冲区
                    pushNext()
                    if (c == '"') break
                }
            }
        }

        addToken(TokenType.STRING)
    }

    /**
     * 读取一列连续的空白符或逗号，并将其丢弃。
     */
    private fun readWhitespaces() {
//        TODO("请实现此方法的剩余部分")
        // 使用 readWhile 和 Char.isWhitespace 方法能简化此函数。
        // 注意，readWhile 会将字符加入缓冲区，由于我们不需要保存空白符，因此在读取完后需要将缓冲区清空。
        readWhile { it.isWhitespace() || it == ',' }
        buffer.clear()
    }

    /**
     * 读取并丢弃注释。
     */
    private fun readComment() {
        if (peekNext() == ';') {
//            TODO("请实现此方法的剩余部分")
            // 使用 readUntil 能简化此函数。
            // 注意，readUntil 会将字符加入缓冲区，由于我们不需要保存注释，因此在读取完后需要将缓冲区清空。
            // 读取直到行尾
            readUntil { it == '\n' }
            index++ // 丢弃掉换行符
            buffer.clear()
        }
    }

    /**
     * 读取一个字符（标签、操作码、立即数、寄存器或数字），然后识别其类型并加入符号串。
     */
    private fun readWord() {
        readUntil { it.isWhitespace() || it == ',' || it == ';' }
        if (buffer.isNotEmpty()) {
            addToken(wordTypeOf(buffer))
        }
    }

    /**
     * 判断符号的类型。可能的取值包括 LABEL OPERATOR IMMEDIATE REGISTER 和 NUMBER。
     */
    private fun wordTypeOf(s: CharSequence): TokenType {
        // 判定符号类型的顺序很重要，因此使用 listOf 而非 setOf
        listOf(
            TokenType.REGISTER to "R[0-7]".toRegex(RegexOption.IGNORE_CASE),
            TokenType.IMMEDIATE to "x[+-]?[0-9A-F]+|#[+-]?[0-9]+".toRegex(RegexOption.IGNORE_CASE),
            TokenType.NUMBER to "[+-]?[0-9]+".toRegex(RegexOption.IGNORE_CASE),
            TokenType.OPERATOR to
                    "ADD|AND|BRN?Z?P?|JMP|JSRR?|LD[IR]?|LEA|NOT|RET|RTI|ST[IR]?|TRAP|\\.(ORIG|END|FILL|BLKW|STRINGZ)|GETC|OUT|IN|PUTS|PUTSP|HALT"
                        .toRegex(RegexOption.IGNORE_CASE)
        ).forEach { (t, r) ->
            if (s.matches(r)) return t
        }
        return TokenType.LABEL
    }

    /**
     * 不断从输入中抽取字符并加入到缓冲区中，直到给定的条件不再满足。
     * 此方法在第一个不满足的字符前停下。
     */
    private fun readWhile(cond: (Char) -> Boolean) {
        while (index < source.length) {
            if (!cond(peekNext())) break
            pushNext()
        }
    }

    /**
     * 不断从输入中抽取字符并加入到缓冲区中，直到给定的条件满足。
     * 此方法在第一个满足的字符前停下。
     */
    private fun readUntil(cond: (Char) -> Boolean) {
        // 注意我们如何组合使用 readWhile 方法来实现此函数的功能，而不是编写一个新的函数
        readWhile { !cond(it) }
    }
}
