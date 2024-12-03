@file:JvmName("Main")

import codegen.CodeGenerator
import linker.createLinker
import parser.createRawInstructions
import tokenizer.Tokenizer
import java.awt.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * 程序的主要入口点，初始化并显示用户界面。
 */
fun main() {
    // 设置 Swing 皮肤，这样可以让界面稍微好看点（
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    // 三排组件，分别显示输入、中间代码和生成代码
    val root = JFrame("Visual LC-3")
    val inputWidget = JTextArea()
    val outputWidget = JTextArea()
    val imInstrWidget = JTextArea()

    // 装载字体
    val ft = Font.createFont(
        Font.TRUETYPE_FONT,
        Thread.currentThread().contextClassLoader.getResourceAsStream("MSYHMONO.ttf"),
    ).deriveFont(16f)

    listOf(inputWidget, outputWidget, imInstrWidget).forEach {
        it.font = ft
        it.lineWrap = true
    }

    // 设置交互属性和样式
    outputWidget.text = "(Nothing)"

    outputWidget.isEditable = false
    imInstrWidget.isEditable = false

    inputWidget.background = Color(0x2a, 0x2a, 0x2a)
    inputWidget.foreground = Color.WHITE
    inputWidget.caretColor = Color.WHITE

    imInstrWidget.background = Color(0x10, 0x10, 0x10)
    imInstrWidget.foreground = Color(0x37, 0xf2, 0xa1)

    outputWidget.background = Color(0x5, 0x5, 0x5)
    outputWidget.foreground = Color(0xff, 0xa8, 0xcf)

    // 当输入框内容变化时，运行汇编器处理源程序
    inputWidget.document.addDocumentListener(object : DocumentListener {
        override fun changedUpdate(e: DocumentEvent?) = update()

        override fun insertUpdate(e: DocumentEvent?) = update()

        override fun removeUpdate(e: DocumentEvent?) = update()

        private fun update() {
            // 以 runCatching 包裹汇编调用，以便捕捉错误
            runCatching {
                val inst = createRawInstructions(Tokenizer(inputWidget.text).resolve())
                imInstrWidget.text = inst.joinToString("\n") { it.toFormatted() }.ifBlank { "(Nothing)" }

                val linker = createLinker(inst)
                val bin = CodeGenerator(inst, linker).build()
                outputWidget.foreground = Color(0xff, 0xa8, 0xcf)
                outputWidget.text = bin.joinToString("\n") { it.toString(2).padStart(16, '0') }.ifBlank { "(Nothing)" }
            }.onFailure {
                imInstrWidget.text = "(Nothing)"
                outputWidget.foreground = Color(0xff, 0x7b, 0x29)
                outputWidget.text = it.toString()
            }
        }
    })

    // 设置布局以摆放组件
    root.layout = GridLayout()
    root.add(JScrollPane(inputWidget))
    root.add(JScrollPane(imInstrWidget))
    root.add(JScrollPane(outputWidget))
    root.pack()
    root.size = optimalWindowSize()
    root.setLocationRelativeTo(null)
    root.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    root.isVisible = true
}

/**
 * 基于屏幕大小为窗口选择合适的长宽。
 */
private fun optimalWindowSize(): Dimension =
    Toolkit.getDefaultToolkit().screenSize.let {
        val expRatio = 16.0 / 9.0
        val devRatio = it.width.toDouble() / it.height
        val h = if (devRatio > expRatio) {
            it.height * 0.6
        } else {
            it.width * 0.6 / expRatio
        }

        Dimension((h * expRatio).toInt(), h.toInt())
    }