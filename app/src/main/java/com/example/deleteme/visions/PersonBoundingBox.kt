package com.example.deleteme.visions

data class PersonBoundingBox(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {

    val area: Long
        get() = width.toLong() * height.toLong()

    val right: Int
        get() = x + width

    val bottom: Int
        get() = y + height
}