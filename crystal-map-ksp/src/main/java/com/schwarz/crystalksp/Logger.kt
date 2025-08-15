package com.schwarz.crystalksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import com.schwarz.crystalcore.ILogger
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class Logger internal constructor(private val logger: KSPLogger) : ILogger<KSNode> {

    private var hasErrors: Boolean = false

    override fun info(msg: String) {
        logger.info(msg)
    }

    override fun info(msg: String, e: KSNode?) {
        logger.info(msg, e)
    }

    override fun warn(msg: String, e: KSNode?) {
      logger.warn(msg, e)
    }

    override fun error(msg: String, e: KSNode?) {
        hasErrors = true
        logger.error(msg, e)
    }

    override fun abortWithError(msg: String, e: KSNode?, ex: Throwable?) {
        error(msg, e)
        throw RuntimeException(ex)
    }

    override fun hasErrors(): Boolean {
        return hasErrors
    }

    override fun abortWithError(msg: String?, elements: List<KSNode>, ex: Throwable?) {
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
