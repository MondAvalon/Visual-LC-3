import codegen.CodeGenerator
import linker.createLinker
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import parser.createRawInstructions
import tokenizer.Tokenizer
import kotlin.test.Test
import kotlin.test.fail

class OnlineTest {
    @Test
    fun `Online Distributed Test`() {
        val cli = OkHttpClient()
        val api = "https://lc3xt.skjsjhb.moe/api/distest/assembler"

        Request.Builder().url(api).build().let {
            cli.newCall(it).execute()
        }.let {
            JSONObject(it.body?.string())
        }.run {
            val session = getString("session")
            val programs = getJSONArray("test")
            val results = JSONArray()
            for (i in 0 until programs.length()) {
                val binary = runCatching {
                    val p = programs.getString(i)
                    val inst = createRawInstructions(Tokenizer(p).resolve())
                    val linker = createLinker(inst)
                    val bin = CodeGenerator(inst, linker).build()
                    bin.joinToString("\n") { it.toString(2).padStart(16, '0') }
                }.getOrElse { "" }
                results.put(binary)
            }

            JSONObject().apply {
                put("session", session)
                put("results", results)
            }
        }.let {
            Request.Builder().url(api).post(it.toString().toRequestBody("application/json".toMediaType())).build()
        }.let {
            cli.newCall(it).execute()
        }.let {
            if (it.code == 418) {
                JSONObject(it.body?.string()).run {
                    println("源程序：")
                    println(getString("source"))
                    println("\n\n期望的输出：")
                    println(getString("expected"))
                    println("\n\n你的输出：")
                    println(getString("received"))
                    fail("有程序未能通过测试")
                }
            } else {
                println("所有程序均测试成功")
            }
        }
    }
}