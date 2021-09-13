package kaufland.com.coachbasebinderapi

@kotlin.annotation.Retention(AnnotationRetention.BINARY)
@kotlin.annotation.Target(AnnotationTarget.CLASS)
annotation class Comment(val comment: Array<String> = [])
