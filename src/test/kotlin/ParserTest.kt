import parser.RawInstruction
import parser.createRawInstructions
import tokenizer.Token
import tokenizer.TokenType
import tokenizer.Tokenizer
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ParserTest {
    @Test
    fun `Parse Completeness Test`() {
        val src = """
        .ORIG x1234
        ADD R0, R1, R2
        AND R3, R4, xa
        BR TAG
        JMP R6
        JSR TAG
        JSRR R6
        LD R0, TAG
        LDI R0, TAG
        LDR R0, R1, x2
        LEA R0, TAG
        NOT R3, R6
        RET
        RTI
        ST R0, TAG
        STI R0, TAG
        STR R0, R1, x2
        TRAP x25
        TAG .FILL x8
        .BLKW 99
        .STRINGZ "ciallo, world"
        GETC
        OUT
        PUTS
        IN
        PUTSP
        HALT
        .END
    """.trimIndent()

        assertContentEquals(
            listOf(
                RawInstruction(emptySet(), ".ORIG", listOf(Token(TokenType.IMMEDIATE, "x1234"))),
                RawInstruction(
                    emptySet(),
                    "ADD",
                    listOf(
                        Token(TokenType.REGISTER, "R0"),
                        Token(TokenType.REGISTER, "R1"),
                        Token(TokenType.REGISTER, "R2")
                    )
                ),
                RawInstruction(
                    emptySet(),
                    "AND",
                    listOf(
                        Token(TokenType.REGISTER, "R3"),
                        Token(TokenType.REGISTER, "R4"),
                        Token(TokenType.IMMEDIATE, "xa")
                    )
                ),
                RawInstruction(emptySet(), "BR", listOf(Token(TokenType.LABEL, "TAG"))),
                RawInstruction(emptySet(), "JMP", listOf(Token(TokenType.REGISTER, "R6"))),
                RawInstruction(emptySet(), "JSR", listOf(Token(TokenType.LABEL, "TAG"))),
                RawInstruction(emptySet(), "JSRR", listOf(Token(TokenType.REGISTER, "R6"))),

                RawInstruction(
                    emptySet(),
                    "LD",
                    listOf(Token(TokenType.REGISTER, "R0"), Token(TokenType.LABEL, "TAG"))
                ),

                RawInstruction(
                    emptySet(),
                    "LDI",
                    listOf(Token(TokenType.REGISTER, "R0"), Token(TokenType.LABEL, "TAG"))
                ),

                RawInstruction(
                    emptySet(),
                    "LDR",
                    listOf(
                        Token(TokenType.REGISTER, "R0"),
                        Token(TokenType.REGISTER, "R1"),
                        Token(TokenType.IMMEDIATE, "x2")
                    )
                ),

                RawInstruction(
                    emptySet(),
                    "LEA",
                    listOf(Token(TokenType.REGISTER, "R0"), Token(TokenType.LABEL, "TAG"))
                ),

                RawInstruction(
                    emptySet(),
                    "NOT",
                    listOf(Token(TokenType.REGISTER, "R3"), Token(TokenType.REGISTER, "R6"))
                ),

                RawInstruction(emptySet(), "RET", emptyList()),
                RawInstruction(emptySet(), "RTI", emptyList()),


                RawInstruction(
                    emptySet(),
                    "ST",
                    listOf(Token(TokenType.REGISTER, "R0"), Token(TokenType.LABEL, "TAG"))
                ),

                RawInstruction(
                    emptySet(),
                    "STI",
                    listOf(Token(TokenType.REGISTER, "R0"), Token(TokenType.LABEL, "TAG"))
                ),

                RawInstruction(
                    emptySet(),
                    "STR",
                    listOf(
                        Token(TokenType.REGISTER, "R0"),
                        Token(TokenType.REGISTER, "R1"),
                        Token(TokenType.IMMEDIATE, "x2")
                    )
                ),

                RawInstruction(emptySet(), "TRAP", listOf(Token(TokenType.IMMEDIATE, "x25"))),
                RawInstruction(setOf("TAG"), ".FILL", listOf(Token(TokenType.IMMEDIATE, "x8"))),
                RawInstruction(emptySet(), ".BLKW", listOf(Token(TokenType.NUMBER, "99"))),
                RawInstruction(emptySet(), ".STRINGZ", listOf(Token(TokenType.STRING, "\"ciallo, world\""))),

                RawInstruction(emptySet(), "TRAP", listOf(Token(TokenType.IMMEDIATE, "x20"))),
                RawInstruction(emptySet(), "TRAP", listOf(Token(TokenType.IMMEDIATE, "x21"))),
                RawInstruction(emptySet(), "TRAP", listOf(Token(TokenType.IMMEDIATE, "x22"))),
                RawInstruction(emptySet(), "TRAP", listOf(Token(TokenType.IMMEDIATE, "x23"))),
                RawInstruction(emptySet(), "TRAP", listOf(Token(TokenType.IMMEDIATE, "x24"))),
                RawInstruction(emptySet(), "TRAP", listOf(Token(TokenType.IMMEDIATE, "x25"))),
                RawInstruction(emptySet(), ".END", emptyList())
            ), createRawInstructions(Tokenizer(src).resolve())
        )
    }

    @Test
    fun `Parse Program`() {
        val src = """
            .ORIG x3000
            LEA R0, TEXT
            PUTS
            HALT
            TEXT .STRINGZ "ciallo, world"
            .END
        """.trimIndent()

        assertContentEquals(
            listOf(
                RawInstruction(emptySet(), ".ORIG", listOf(Token(TokenType.IMMEDIATE, "x3000"))),
                RawInstruction(
                    emptySet(),
                    "LEA",
                    listOf(Token(TokenType.REGISTER, "R0"), Token(TokenType.LABEL, "TEXT"))
                ),
                RawInstruction(emptySet(), "TRAP", listOf(Token(TokenType.IMMEDIATE, "x22"))),
                RawInstruction(emptySet(), "TRAP", listOf(Token(TokenType.IMMEDIATE, "x25"))),
                RawInstruction(setOf("TEXT"), ".STRINGZ", listOf(Token(TokenType.STRING, "\"ciallo, world\""))),
                RawInstruction(emptySet(), ".END", emptyList())
            ), createRawInstructions(Tokenizer(src).resolve())
        )
    }
}