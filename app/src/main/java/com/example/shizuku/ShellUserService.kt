package com.example.shizuku

import com.example.IShellService
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class ShellUserService : IShellService.Stub() {

    override fun exec(command: String?): String {
        if (command.isNullOrBlank()) return "__EXIT__:0\n"
        return try {
            val process = ProcessBuilder("/system/bin/sh", "-c", command)
                .redirectErrorStream(true)
                .start()

            val output = StringBuilder()
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                var lineCount = 0
                while (reader.readLine().also { line = it } != null && lineCount < 4000) {
                    output.append(line).append('\n')
                    lineCount++
                }
            }

            val finished = process.waitFor(20, TimeUnit.SECONDS)
            val exitCode = if (finished) {
                process.exitValue()
            } else {
                process.destroyForcibly()
                124
            }
            "__EXIT__:$exitCode\n$output"
        } catch (t: Throwable) {
            "__EXIT__:1\n${t.message ?: "Shell execution error"}\n"
        }
    }

    override fun getUid(): Int {
        return android.os.Process.myUid()
    }

    override fun destroy() {
        exitProcess(0)
    }
}
