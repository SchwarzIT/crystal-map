package com.schwarz.crystaldemo.customtypes

class GenerateClassName(val name: String = GenerateClassName::class.simpleName ?: "") {

    override fun toString() = name
}
