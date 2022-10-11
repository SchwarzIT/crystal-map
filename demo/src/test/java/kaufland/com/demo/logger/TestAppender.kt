package kaufland.com.demo.logger

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase

class TestAppender : AppenderBase<ILoggingEvent>() {
    private val events: MutableList<ILoggingEvent> = mutableListOf()

    val lastLoggedEvent
        get() = events.lastOrNull()

    override fun append(eventObject: ILoggingEvent) {
        events.add(eventObject)
    }
}