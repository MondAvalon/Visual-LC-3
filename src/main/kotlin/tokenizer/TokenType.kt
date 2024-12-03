package tokenizer

/**
 * 符号的类型
 */
enum class TokenType {
    /**
     * 指令和伪指令操作码
     */
    OPERATOR,

    /**
     * 寄存器
     */
    REGISTER,

    /**
     * 标签
     */
    LABEL,

    /**
     * 带前缀的立即数
     */
    IMMEDIATE,

    /**
     * 无前缀的数字（用于 BLKW）
     */
    NUMBER,

    /**
     * 带引号的字符串
     */
    STRING,
}