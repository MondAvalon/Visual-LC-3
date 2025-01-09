import linker.createLinker
import parser.createRawInstructions
import tokenizer.Tokenizer
import kotlin.test.Test
import kotlin.test.assertEquals

class LinkerTest {
    @Test
    fun `Linker Completeness Test`() {
        val src = """
            .ORIG x200
            LEA R0, TEXT
            TEXT .BLKW 3
            NUM .STRINGZ "ciallo"
            A .FILL x0
            B .FILL x1
            .END
        """.trimIndent()
        createLinker(createRawInstructions(Tokenizer(src).resolve())).run {
            assertEquals(0x201, getLabelAddress("TEXT"))
            assertEquals(0x204, getLabelAddress("NUM"))
            assertEquals(0x20b, getLabelAddress("A"))
            assertEquals(0x20c, getLabelAddress("B"))
        }
    }
}