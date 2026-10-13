package com.example.currencyconverter.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.currencyconverter.data.model.Currency
import com.example.currencyconverter.data.model.CurrencyKind
import com.example.currencyconverter.domain.CurrencyConverter
import com.example.currencyconverter.ui.MainViewModel
import com.example.currencyconverter.ui.components.CurrencyFlag
import com.example.currencyconverter.util.DetectedAmount
import com.example.currencyconverter.util.findLargestAmount
import com.example.currencyconverter.util.formatAmount
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@Composable
fun CameraConvertScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var detectedAmount by remember { mutableStateOf<DetectedAmount?>(null) }
    var cameraMessage by remember { mutableStateOf<String?>(null) }
    var lastDetectionMillis by remember { mutableLongStateOf(0L) }
    var photoRequestId by remember { mutableIntStateOf(0) }

    var cameraPermissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraPermissionGranted = granted
        if (!granted) cameraMessage = "Recognition cannot start without camera access."
    }

    val currencies = vm.realCurrencies + vm.customAndFunCurrencies
    val currencyIds = currencies.map { it.id }
    var fromId by rememberSaveable { mutableStateOf("EUR") }
    var toId by rememberSaveable { mutableStateOf("USD") }
    LaunchedEffect(currencyIds) {
        if (currencyIds.isNotEmpty()) {
            if (fromId !in currencyIds) fromId = currencies.firstOrNull { it.id == "EUR" }?.id ?: currencyIds.first()
            if (toId !in currencyIds || toId == fromId) {
                toId = currencies.firstOrNull { it.id == "USD" && it.id != fromId }?.id
                    ?: currencyIds.firstOrNull { it != fromId }
                    ?: fromId
            }
        }
    }
    val fromCurrency = currencies.firstOrNull { it.id == fromId }
    val toCurrency = currencies.firstOrNull { it.id == toId }

    val onLiveText: (String) -> Unit = { text ->
        if (photoBitmap == null) {
            val candidate = findLargestAmount(text)
            if (candidate != null) {
                detectedAmount = candidate
                lastDetectionMillis = SystemClock.elapsedRealtime()
                cameraMessage = null
            } else if (SystemClock.elapsedRealtime() - lastDetectionMillis > 1800L) {
                detectedAmount = null
            }
        }
    }

    DisposableEffect(recognizer) {
        onDispose { recognizer.close() }
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CurrencyPicker("From", currencies, fromId, { fromId = it }, Modifier.weight(1f))
            IconButton(
                onClick = { val previousFrom = fromId; fromId = toId; toId = previousFrom },
                modifier = Modifier.size(44.dp),
                enabled = currencies.size > 1
            ) {
                Text("⇄", fontSize = 26.sp, color = MaterialTheme.colorScheme.primary)
            }
            CurrencyPicker("To", currencies, toId, { toId = it }, Modifier.weight(1f))
        }

        Box(
            Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (cameraPermissionGranted) {
                CameraPreview(
                    lifecycleOwner = lifecycleOwner,
                    recognizer = recognizer,
                    photoMode = photoBitmap != null,
                    onTextRecognized = onLiveText,
                    onPreviewReady = { previewView = it },
                    onCameraError = { cameraMessage = it },
                    modifier = Modifier.fillMaxSize()
                )
                photoBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Captured camera image, held temporarily in memory only",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera access required", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("The camera is used only for live text recognition and the optional photo.", textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Allow camera")
                        }
                    }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Recognized conversion", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                val converted = if (detectedAmount != null && fromCurrency != null && toCurrency != null) {
                    CurrencyConverter(vm.rates).convert(detectedAmount!!.value, fromCurrency, toCurrency)
                } else null
                if (detectedAmount != null && fromCurrency != null && toCurrency != null && converted != null) {
                    Text(
                        "${detectedAmount!!.display} ${fromCurrency.id} = ${formatAmount(converted)} ${toCurrency.id}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                } else {
                    Text(
                        when {
                            currencies.isEmpty() -> "Load rates to choose currencies."
                            detectedAmount == null -> "Point the camera at an amount. The largest detected number is used."
                            else -> "Conversion is currently unavailable for these currencies."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                cameraMessage?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (photoBitmap == null) {
                OutlinedButton(
                    onClick = {
                        val frame = previewView?.bitmap
                        if (frame == null) {
                            cameraMessage = "The camera image could not be captured."
                        } else {
                            photoBitmap = frame
                            detectedAmount = null
                            cameraMessage = "Analyzing photo on this device …"
                            val requestId = ++photoRequestId
                            recognizer.process(InputImage.fromBitmap(frame, 0))
                                .addOnSuccessListener { result ->
                                    if (photoRequestId == requestId && photoBitmap != null) {
                                        detectedAmount = findLargestAmount(result.text)
                                        cameraMessage = if (detectedAmount == null) {
                                            "No number detected. Reposition the camera and try again."
                                        } else null
                                    }
                                }
                                .addOnFailureListener {
                                    if (photoRequestId == requestId && photoBitmap != null) {
                                        cameraMessage = "The photo could not be read."
                                    }
                                }
                        }
                    },
                    enabled = cameraPermissionGranted
                ) { Text("Analyze photo") }
            } else {
                Button(onClick = {
                    photoRequestId++
                    photoBitmap = null
                    detectedAmount = null
                    cameraMessage = "Photo discarded. Live recognition continues."
                }) { Text("Discard photo") }
            }
            Text(
                "Text recognition runs on this device. Photos are not saved to your gallery.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CurrencyPicker(
    label: String,
    currencies: List<Currency>,
    selectedId: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = currencies.firstOrNull { it.id == selectedId }

    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
        Box {
            OutlinedButton(onClick = { expanded = true }, enabled = currencies.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                if (selected != null) CurrencyFlag(selected)
                Text(
                        selected?.let { "${it.name} (${it.id})" } ?: "Currency",
                    modifier = Modifier.weight(1f).padding(start = 6.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("⌄")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 360.dp)
            ) {
                currencies.forEach { currency ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CurrencyFlag(currency)
                                Column(Modifier.padding(start = 8.dp)) {
                                    Text(currency.name)
                                    Text(
                                        if (currency.kind == CurrencyKind.REAL) currency.id else currency.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = { onSelected(currency.id); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalGetImage::class)
private fun CameraPreview(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    photoMode: Boolean,
    onTextRecognized: (String) -> Unit,
    onPreviewReady: (PreviewView) -> Unit,
    onCameraError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val executor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val latestPhotoMode = rememberUpdatedState(photoMode)
    val latestTextCallback = rememberUpdatedState(onTextRecognized)
    val latestErrorCallback = rememberUpdatedState(onCameraError)
    val processing = remember { AtomicBoolean(false) }
    val lastFrameAt = remember { AtomicLong(0L) }
    val analyzer = remember(recognizer) {
        ImageAnalysis.Analyzer { imageProxy ->
            val now = SystemClock.elapsedRealtime()
            if (latestPhotoMode.value || now - lastFrameAt.get() < 250L || !processing.compareAndSet(false, true)) {
                imageProxy.close()
            } else {
                lastFrameAt.set(now)
                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    processing.set(false)
                    imageProxy.close()
                } else {
                    try {
                        recognizer.process(InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees))
                            .addOnSuccessListener { result -> latestTextCallback.value(result.text) }
                            .addOnFailureListener { latestErrorCallback.value("Live text recognition is currently unavailable.") }
                            .addOnCompleteListener {
                                imageProxy.close()
                                processing.set(false)
                            }
                    } catch (e: Exception) {
                        imageProxy.close()
                        processing.set(false)
                        latestErrorCallback.value("Live text recognition is currently unavailable.")
                    }
                }
            }
        }
    }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    AndroidView(
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { view ->
            if (previewView !== view) {
                previewView = view
                onPreviewReady(view)
            }
        },
        modifier = modifier
    )

    DisposableEffect(lifecycleOwner, previewView) {
        var cameraProvider: ProcessCameraProvider? = null
        val active = AtomicBoolean(true)
        val view = previewView
        if (view != null) {
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener({
                if (active.get()) {
                    try {
                        val provider = providerFuture.get()
                        cameraProvider = provider
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
                        // Small shelf-label punctuation is easy to lose at the default
                        // analyzer resolution. Request a sharper frame for OCR.
                        val resolutionSelector = ResolutionSelector.Builder()
                            .setResolutionStrategy(
                                ResolutionStrategy(
                                    Size(1280, 960),
                                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                                )
                            )
                            .build()
                        val analysis = ImageAnalysis.Builder()
                            .setResolutionSelector(resolutionSelector)
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        analysis.setAnalyzer(executor, analyzer)
                        provider.unbindAll()
                        provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                    } catch (e: Exception) {
                        latestErrorCallback.value("The rear camera could not be started.")
                    }
                }
            }, ContextCompat.getMainExecutor(context))
        }
        onDispose {
            active.set(false)
            cameraProvider?.unbindAll()
        }
    }

    DisposableEffect(executor) {
        onDispose { executor.shutdown() }
    }
}
