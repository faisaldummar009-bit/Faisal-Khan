package com.example.ui.screens.imageedit

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.GeminiRepository
import com.example.data.repository.ImageRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

data class ImageEditUiState(
    val originalUri: Uri? = null,
    val originalBase64: String? = null,
    val editPrompt: String = "",
    val isEditing: Boolean = false,
    val editedBase64: String? = null,
    val showComparisonOriginal: Boolean = false,
    val error: String? = null
)

class ImageEditViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val imageRepo = ImageRepository(db.imageDao())
    private val geminiRepo = GeminiRepository()
    private val prefsRepo = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(ImageEditUiState())
    val uiState: StateFlow<ImageEditUiState> = _uiState.asStateFlow()

    fun setOriginalImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    val maxDim = 1024
                    val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                        val w = if (ratio >= 1) maxDim else (maxDim * ratio).toInt()
                        val h = if (ratio >= 1) (maxDim / ratio).toInt() else maxDim
                        Bitmap.createScaledBitmap(bitmap, w, h, true)
                    } else {
                        bitmap
                    }
                    scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    _uiState.value = _uiState.value.copy(
                        originalUri = uri,
                        originalBase64 = base64,
                        editedBase64 = null,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error loading image: ${e.message}")
            }
        }
    }

    fun updatePrompt(text: String) {
        _uiState.value = _uiState.value.copy(editPrompt = text)
    }

    fun toggleComparison(showOriginal: Boolean) {
        _uiState.value = _uiState.value.copy(showComparisonOriginal = showOriginal)
    }

    fun applyEdit() {
        val base64 = _uiState.value.originalBase64 ?: return
        val prompt = _uiState.value.editPrompt.trim()
        if (prompt.isBlank()) return

        _uiState.value = _uiState.value.copy(isEditing = true, error = null)

        viewModelScope.launch {
            val customKey = prefsRepo.customApiKey.firstOrNull()
            val effectiveKey = prefsRepo.getEffectiveApiKey(customKey)

            val result = geminiRepo.editImage(
                apiKey = effectiveKey,
                originalBase64 = base64,
                editPrompt = prompt
            )

            result.onSuccess { newBase64 ->
                imageRepo.saveImage(
                    prompt = "Edit: $prompt",
                    aspectRatio = "1:1",
                    style = "Edited",
                    imageBase64 = newBase64
                )
                prefsRepo.incrementUsage()
                _uiState.value = _uiState.value.copy(
                    isEditing = false,
                    editedBase64 = newBase64
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isEditing = false,
                    error = err.message ?: "Image editing failed"
                )
            }
        }
    }

    fun saveEditedImage(context: Context) {
        val base64 = _uiState.value.editedBase64 ?: return
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return

            val filename = "LightGPT_Edit_${System.currentTimeMillis()}.jpg"
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
                val lightGptDir = java.io.File(imagesDir, "LightGPT").apply { mkdirs() }
                val imageFile = java.io.File(lightGptDir, filename)
                outputStream = java.io.FileOutputStream(imageFile)
            }

            outputStream?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            Toast.makeText(context, "Edited image saved to Pictures!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
