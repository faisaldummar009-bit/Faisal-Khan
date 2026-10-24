package com.example.ui.screens.imagegen

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.GeneratedImageEntity
import com.example.data.repository.GeminiRepository
import com.example.data.repository.ImageRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

data class ImageGenUiState(
    val prompt: String = "",
    val selectedAspectRatio: String = "1:1",
    val selectedStyle: String = "Realistic",
    val isGenerating: Boolean = false,
    val currentGeneratedBase64: String? = null,
    val error: String? = null
)

class ImageGenViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val imageRepo = ImageRepository(db.imageDao())
    private val geminiRepo = GeminiRepository()
    private val prefsRepo = UserPreferencesRepository(application)

    val savedGallery: StateFlow<List<GeneratedImageEntity>> = imageRepo.allImages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ImageGenUiState())
    val uiState: StateFlow<ImageGenUiState> = _uiState.asStateFlow()

    fun updatePrompt(text: String) {
        _uiState.value = _uiState.value.copy(prompt = text)
    }

    fun updateAspectRatio(ratio: String) {
        _uiState.value = _uiState.value.copy(selectedAspectRatio = ratio)
    }

    fun updateStyle(style: String) {
        _uiState.value = _uiState.value.copy(selectedStyle = style)
    }

    fun generateImage() {
        val prompt = _uiState.value.prompt.trim()
        if (prompt.isBlank()) return

        _uiState.value = _uiState.value.copy(isGenerating = true, error = null)

        viewModelScope.launch {
            val customKey = prefsRepo.customApiKey.firstOrNull()
            val effectiveKey = prefsRepo.getEffectiveApiKey(customKey)

            val result = geminiRepo.generateImage(
                apiKey = effectiveKey,
                prompt = prompt,
                aspectRatio = _uiState.value.selectedAspectRatio,
                style = _uiState.value.selectedStyle
            )

            result.onSuccess { base64 ->
                // Save to local database
                imageRepo.saveImage(
                    prompt = prompt,
                    aspectRatio = _uiState.value.selectedAspectRatio,
                    style = _uiState.value.selectedStyle,
                    imageBase64 = base64
                )
                prefsRepo.incrementUsage()
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    currentGeneratedBase64 = base64
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    error = err.message ?: "Failed to generate image"
                )
            }
        }
    }

    fun deleteImage(id: String) {
        viewModelScope.launch {
            imageRepo.deleteImage(id)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun saveToDeviceGallery(context: Context, base64: String) {
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return

            val filename = "LightGPT_${System.currentTimeMillis()}.jpg"
            var outputStream: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LightGPT")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    outputStream = resolver.openOutputStream(imageUri)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val lightGptDir = File(imagesDir, "LightGPT").apply { mkdirs() }
                val imageFile = File(lightGptDir, filename)
                outputStream = FileOutputStream(imageFile)
            }

            outputStream?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            Toast.makeText(context, "Saved to Pictures/LightGPT!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareImage(context: Context, base64: String) {
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val cachePath = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(cachePath, "shared_light_gpt.jpg")
            FileOutputStream(file).use { it.write(bytes) }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share generated image"))
        } catch (e: Exception) {
            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
