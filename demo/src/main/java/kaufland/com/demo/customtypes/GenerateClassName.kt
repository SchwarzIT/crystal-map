package kaufland.com.demo.customtypes

class GenerateClassName(val name: String = GenerateClassName::class.simpleName ?: "") {

    override fun toString() = name
}
