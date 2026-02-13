package com.papermill.maintenance.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.papermill.maintenance.R
import com.papermill.maintenance.di.AppModule
import com.papermill.maintenance.ui.theme.*
import com.papermill.maintenance.ui.viewmodel.RecordDetailViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(
    recordId: Long,
    onNavigateBack: () -> Unit,
    viewModel: RecordDetailViewModel = viewModel(
        factory = RecordDetailViewModel.Factory(
            AppModule.getRepository(LocalContext.current)
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var voiceInputTarget by remember { mutableStateOf<VoiceInputTarget?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            viewModel.addImage(tempPhotoUri.toString())
        }
    }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addImage(it.toString()) }
    }
    
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        results?.firstOrNull()?.let { text ->
            when (voiceInputTarget) {
                VoiceInputTarget.TITLE -> viewModel.onTitleChange(uiState.title + text)
                VoiceInputTarget.CONTENT -> viewModel.onContentChange(uiState.content + text)
                VoiceInputTarget.EQUIPMENT -> viewModel.onEquipmentNameChange(uiState.equipmentName + text)
                null -> {}
            }
        }
        voiceInputTarget = null
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera(context, cameraLauncher)
        } else {
            Toast.makeText(context, context.getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
        }
    }
    
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition(context, speechRecognizerLauncher)
        } else {
            Toast.makeText(context, context.getString(R.string.microphone_permission_required), Toast.LENGTH_SHORT).show()
        }
    }
    
    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }
    
    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) {
            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (uiState.isNewRecord) stringResource(R.string.add_record) 
                        else stringResource(R.string.edit_record)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = SurfaceColor
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回", tint = SurfaceColor)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveRecord() },
                        enabled = uiState.title.isNotBlank() && !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = SurfaceColor
                            )
                        } else {
                            Icon(Icons.Default.Save, stringResource(R.string.save), tint = SurfaceColor)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text(stringResource(R.string.title_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        voiceInputTarget = VoiceInputTarget.TITLE
                        checkAndRequestMicrophonePermission(
                            context, 
                            microphonePermissionLauncher
                        ) {
                            startVoiceRecognition(context, speechRecognizerLauncher)
                        }
                    }) {
                        Icon(Icons.Default.Mic, stringResource(R.string.voice_input))
                    }
                }
            )
            
            OutlinedTextField(
                value = uiState.equipmentName,
                onValueChange = { viewModel.onEquipmentNameChange(it) },
                label = { Text(stringResource(R.string.equipment_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        voiceInputTarget = VoiceInputTarget.EQUIPMENT
                        checkAndRequestMicrophonePermission(
                            context,
                            microphonePermissionLauncher
                        ) {
                            startVoiceRecognition(context, speechRecognizerLauncher)
                        }
                    }) {
                        Icon(Icons.Default.Mic, stringResource(R.string.voice_input))
                    }
                }
            )
            
            OutlinedTextField(
                value = uiState.content,
                onValueChange = { viewModel.onContentChange(it) },
                label = { Text(stringResource(R.string.content_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                trailingIcon = {
                    IconButton(onClick = {
                        voiceInputTarget = VoiceInputTarget.CONTENT
                        checkAndRequestMicrophonePermission(
                            context,
                            microphonePermissionLauncher
                        ) {
                            startVoiceRecognition(context, speechRecognizerLauncher)
                        }
                    }) {
                        Icon(Icons.Default.Mic, stringResource(R.string.voice_input))
                    }
                }
            )
            
            Text(
                text = stringResource(R.string.images_label),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        checkAndRequestCameraPermission(
                            context,
                            cameraPermissionLauncher
                        ) {
                            launchCamera(context, cameraLauncher)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.take_photo))
                }
                
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.select_image))
                }
            }
            
            if (uiState.imagePaths.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.imagePaths.forEach { imagePath ->
                        ImageThumbnail(
                            imagePath = imagePath,
                            onRemove = { viewModel.removeImage(imagePath) }
                        )
                    }
                }
            }
            
            if (!uiState.isNewRecord) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                Text(
                    text = "${stringResource(R.string.time_label)}: ${dateFormat.format(Date(uiState.id))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

private enum class VoiceInputTarget {
    TITLE, CONTENT, EQUIPMENT
}

@Composable
private fun ImageThumbnail(
    imagePath: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier.size(100.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imagePath)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .background(Error, RoundedCornerShape(50))
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "删除图片",
                tint = SurfaceColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun checkAndRequestCameraPermission(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<String>,
    onGranted: () -> Unit
) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
        == PackageManager.PERMISSION_GRANTED) {
        onGranted()
    } else {
        launcher.launch(Manifest.permission.CAMERA)
    }
}

private fun checkAndRequestMicrophonePermission(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<String>,
    onGranted: () -> Unit
) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
        == PackageManager.PERMISSION_GRANTED) {
        onGranted()
    } else {
        launcher.launch(Manifest.permission.RECORD_AUDIO)
    }
}

private fun launchCamera(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<Uri>
) {
    val photoFile = File(
        context.cacheDir,
        "photo_${System.currentTimeMillis()}.jpg"
    )
    val photoUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile
    )
    launcher.launch(photoUri)
}

private fun startVoiceRecognition(
    context: Context,
    launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
) {
    val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出内容")
    }
    launcher.launch(intent)
}
