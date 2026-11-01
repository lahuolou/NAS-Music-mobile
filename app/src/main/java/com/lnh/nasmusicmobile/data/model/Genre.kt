package com.lnh.nasmusicmobile.data.model

/**
 * 音乐流派/风格
 */
data class Genre(
    val id: String,
    val name: String,
    val songCount: Int = 0
)
