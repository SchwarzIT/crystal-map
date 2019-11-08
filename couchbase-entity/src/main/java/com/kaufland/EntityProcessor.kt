package com.kaufland

import com.squareup.kotlinpoet.FileSpec

import javax.lang.model.element.Element

interface EntityProcessor {

    fun process(element: Element): FileSpec

}
