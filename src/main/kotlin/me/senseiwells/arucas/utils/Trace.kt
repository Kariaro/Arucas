package me.senseiwells.arucas.utils

abstract class Trace(val fileName: String) {
    companion object {
        @JvmStatic
        val INTERNAL = InternalTrace(null)
    }

    override fun toString(): String {
        return "File: ${this.fileName}"
    }
}

open class LocatableTrace(fileName: String, val line: Int, val column: Int): Trace(fileName) {
    override fun toString(): String {
        return "${super.toString()}, Line: ${this.line + 1}, Column: ${this.column + 1}"
    }
}

open class CallTrace(fileName: String, line: Int, column: Int, private val callName: String): LocatableTrace(fileName, line, column) {
    constructor(trace: LocatableTrace, string: String): this(trace.fileName, trace.line, trace.column, string)

    override fun toString(): String {
        return "${super.toString()}, In: ${this.callName}"
    }
}

class InternalTrace(private val details: String?): CallTrace("\$internal", 0, 0, "") {
    override fun toString(): String {
        return "File: ${this.fileName}${if (this.details == null) "" else ", ${this.details}"}"
    }
}