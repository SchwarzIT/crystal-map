package kaufland.com.coachbasebinderapi


import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import kotlin.reflect.KClass

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
annotation class Comment(val comment: Array<String> = [])
