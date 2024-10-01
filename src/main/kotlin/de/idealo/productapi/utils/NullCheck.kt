package de.idealo.productapi.utils

fun <T : Any> allNotNull(vararg elements: T?): List<T>? = if(elements.contains(null)) null else elements.filterNotNull()
fun <T : Any> makeListWithNotNullEl(vararg elements: T?): List<T> = elements.filterNotNull()