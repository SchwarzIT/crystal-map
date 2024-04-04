package com.schwarz.crystalapi.schema

interface CMType

class CMField<T: Any>(val name: String, val path: String) : CMType

class CMList<T: Any>(val name: String, val path: String) : CMType

class CMObject<out T : Schema>(val element: T, val path: String) : CMType

class CMObjectList<out T : Schema>(val element: T, val name: String, val path: String) : CMType
