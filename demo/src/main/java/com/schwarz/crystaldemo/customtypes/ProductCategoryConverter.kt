package com.schwarz.crystaldemo.customtypes

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter
import com.schwarz.crystalapi.typeconverters.EnumConverter
import com.schwarz.crystaldemo.entity.ProductCategory

@TypeConverter
abstract class ProductCategoryConverter : ITypeConverter<ProductCategory, String> by EnumConverter(ProductCategory::class)
