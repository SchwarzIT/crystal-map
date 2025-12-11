package com.schwarz.crystalprocessor

import com.schwarz.crystalcore.ILogger
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class Logger internal constructor(
    processingEnv: ProcessingEnvironment,
) : ILogger<Element> {
    private val messager: Messager

    private var hasErrors: Boolean = false

    init {
        this.messager = processingEnv.messager
    }

    override fun info(msg: String) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg)
    }

    override fun info(
        msg: String,
        e: Element?,
    ) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg, e)
    }

    override fun warn(
        msg: String,
        e: Element?,
    ) {
        messager.printMessage(Diagnostic.Kind.WARNING, msg, e)
    }

    override fun error(
        msg: String,
        e: Element?,
    ) {
        hasErrors = true
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e)
    }

    override fun abortWithError(
        msg: String,
        e: Element?,
        ex: Throwable?,
    ) {
        error(msg, e)
        throw RuntimeException(ex)
    }

    override fun hasErrors(): Boolean = hasErrors

    override fun abortWithError(
        msg: String?,
        elements: List<Element>,
        ex: Throwable?,
    ) {
        if (elements.isNotEmpty()) {
            for (causingElement in elements) {
                error(msg ?: "unknown", causingElement)
            }
        } else {
            error(msg ?: "unknown", null)
        }
        if (ex != null) {
            throw ex
        }
        throw Exception("Validation failed")
    }
}
