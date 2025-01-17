package me.senseiwells.arucas.api

import java.util.Scanner
import java.util.concurrent.CompletableFuture

const val BOLD = "\u001b[1;31m"
const val DEBUG = "\u001b[1;36m"
const val ERROR = "\u001b[0;31m"
const val RESET = "\u001b[0m"

open class ImplArucasIO(
    private val format: Boolean = true
): ArucasInput, ArucasOutput {
    private val scanner = Scanner(System.`in`)

    override fun takeInput(): CompletableFuture<String> {
        return CompletableFuture.completedFuture(this.scanner.next())
    }

    override fun formatError(string: String) = if (this.format) ERROR + string + RESET else string

    override fun formatErrorBold(string: String) = if (this.format) BOLD + string + RESET else string

    override fun print(value: Any?) = kotlin.io.print(value)

    override fun log(value: Any?) = this.print(if (this.format) "$DEBUG[DEBUG]$RESET $value" else "[DEBUG] $value")
}