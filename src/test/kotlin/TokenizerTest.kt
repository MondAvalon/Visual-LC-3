import tokenizer.Token
import tokenizer.TokenType
import tokenizer.Tokenizer
import kotlin.test.Test
import kotlin.test.assertContentEquals

class TokenizerTest {
    @Test
    fun `Tokenize Completeness Test`() {
        val src = """
            ADD AND BRN BRZ BRP BRNZ BRNP BRZP BRNZP BR JMP JSR JSRR
            LD LDI LDR LEA NOT RET RTI ST STI STR TRAP .ORIG .END .FILL
            .BLKW .STRINGZ GETC OUT IN PUTS PUTSP HALT
            R0 R1 R2 R3 R4 R5 R6 R7 R8 R9
            MY_LABEL SPECIAL/LABEL NOT-ADD-LABEL LOOP,LABEL
            X123a,X456b,x789c,xd0ef,x-55aa
            #1234, #5678, #9000, #-0721, #-1145
            0 -1 42 ; 8888
            "ciallo, world" "Escaped \" Quote"
        """.trimIndent()

        assertContentEquals(
            listOf(
                "ADD", "AND", "BRN", "BRZ", "BRP", "BRNZ", "BRNP",
                "BRZP", "BRNZP", "BR", "JMP", "JSR", "JSRR", "LD", "LDI",
                "LDR", "LEA", "NOT", "RET", "RTI", "ST", "STI", "STR", "TRAP",
                ".ORIG", ".END", ".FILL", ".BLKW", ".STRINGZ", "GETC", "OUT", "IN",
                "PUTS", "PUTSP", "HALT"
            ).map { Token(TokenType.OPERATOR, it) } +
                    listOf("R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7").map { Token(TokenType.REGISTER, it) } +
                    listOf("R8", "R9", "MY_LABEL", "SPECIAL/LABEL", "NOT-ADD-LABEL", "LOOP", "LABEL")
                        .map { Token(TokenType.LABEL, it) } +
                    listOf("X123a", "X456b", "x789c", "xd0ef", "x-55aa", "#1234", "#5678", "#9000", "#-0721", "#-1145")
                        .map { Token(TokenType.IMMEDIATE, it) } +
                    listOf("0", "-1", "42").map { Token(TokenType.NUMBER, it) } +
                    listOf("\"ciallo, world\"", "\"Escaped \" Quote\"").map { Token(TokenType.STRING, it) },
            Tokenizer(src).resolve()
        )
    }

    @Test
    fun `Tokenize Formatted Source`() {
        val src = """
            .ORIG x3000
            LABEL AND R0, R1, R2 ; Comment
            LABEL_2 HALT
            .STRINGZ "ciallo, world\n"
            .END
        """.trimIndent()

        assertContentEquals(
            listOf(
                Token(TokenType.OPERATOR, ".ORIG"),
                Token(TokenType.IMMEDIATE, "x3000"),
                Token(TokenType.LABEL, "LABEL"),
                Token(TokenType.OPERATOR, "AND"),
                Token(TokenType.REGISTER, "R0"),
                Token(TokenType.REGISTER, "R1"),
                Token(TokenType.REGISTER, "R2"),
                Token(TokenType.LABEL, "LABEL_2"),
                Token(TokenType.OPERATOR, "HALT"),
                Token(TokenType.OPERATOR, ".STRINGZ"),
                Token(TokenType.STRING, "\"ciallo, world\n\""),
                Token(TokenType.OPERATOR, ".END"),
            ),
            Tokenizer(src).resolve()
        )
    }

    @Test
    fun `Tokenize Complex Source`() {
        val src = """
            ADDR0 BRnz MY-LABEL,R9;Comment
            MY-LABEL .BLKW 5
        """.trimIndent()

        assertContentEquals(
            listOf(
                Token(TokenType.LABEL, "ADDR0"),
                Token(TokenType.OPERATOR, "BRnz"),
                Token(TokenType.LABEL, "MY-LABEL"),
                Token(TokenType.LABEL, "R9"),
                Token(TokenType.LABEL, "MY-LABEL"),
                Token(TokenType.OPERATOR, ".BLKW"),
                Token(TokenType.NUMBER, "5"),
            ),
            Tokenizer(src).resolve()
        )
    }
}