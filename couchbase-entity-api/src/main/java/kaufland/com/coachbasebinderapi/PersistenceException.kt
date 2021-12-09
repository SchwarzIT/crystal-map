package kaufland.com.coachbasebinderapi

import java.lang.Exception

class PersistenceException : Exception {
    constructor(var1: Throwable?) : super(var1)
    constructor(message: String?) : super(message)
}
