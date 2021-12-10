package kaufland.com.demo.entity

import kaufland.com.coachbasebinderapi.TypeConversion

enum class ProductCategory {
    GREAT_PRODUCT,
    AMAZING_PRODUCT
}

object ProductCategoryTypeConversion : TypeConversion {
    override fun write(value: Any?): Any? = value?.let {
        when (it) {
            is String -> it
            is ProductCategory -> it.name
            else -> null
        }
    }

    override fun read(value: Any?): Any? = value.let {
        when (it) {
            is String -> ProductCategory.valueOf(it)
            is ProductCategory -> it
            else -> null
        }
    }
}
