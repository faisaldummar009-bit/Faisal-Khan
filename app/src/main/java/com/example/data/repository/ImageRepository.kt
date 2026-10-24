package com.example.data.repository

import com.example.data.local.dao.ImageDao
import com.example.data.local.entity.GeneratedImageEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ImageRepository(private val imageDao: ImageDao) {

    val allImages: Flow<List<GeneratedImageEntity>> = imageDao.getAllImages()

    suspend fun saveImage(
        prompt: String,
        aspectRatio: String,
        style: String,
        imageBase64: String
    ): String {
        val id = UUID.randomUUID().toString()
        val entity = GeneratedImageEntity(
            id = id,
            prompt = prompt,
            aspectRatio = aspectRatio,
            style = style,
            imageBase64 = imageBase64,
            createdAt = System.currentTimeMillis()
        )
        imageDao.insertImage(entity)
        return id
    }

    suspend fun deleteImage(id: String) {
        imageDao.deleteImageById(id)
    }

    suspend fun clearAll() {
        imageDao.clearAllImages()
    }
}
