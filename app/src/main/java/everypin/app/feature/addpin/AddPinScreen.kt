package everypin.app.feature.addpin

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import everypin.app.R
import everypin.app.core.extension.removeTempImagesDir
import everypin.app.core.ui.component.CommonAsyncImage
import everypin.app.core.ui.component.dialog.ImageAddMenuDialog
import everypin.app.core.ui.theme.EveryPinTheme
import everypin.app.core.utils.FileUtil
import everypin.app.data.model.PlaceInfo
import everypin.app.feature.search.SearchPlaceActivity
import kotlinx.coroutines.launch
import logcat.LogPriority
import logcat.asLog
import logcat.logcat

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun AddPinRoute(
    addPinViewModel: AddPinViewModel = hiltViewModel(),
    onShowSnackbar: suspend (String, String?) -> SnackbarResult
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val selectedImageList by addPinViewModel.selectedImageListState.collectAsStateWithLifecycle()
    val pinState by addPinViewModel.pinState.collectAsStateWithLifecycle()
    var showRegPinErrorDialog by remember { mutableStateOf(false) }
    var showRegPinSuccessDialog by remember { mutableStateOf(false) }
    var regPinErrorMessage by remember { mutableStateOf("") }
    val searchPlaceLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val placeInfo = data?.let {
                IntentCompat.getParcelableExtra(
                    it,
                    SearchPlaceActivity.ExtraKey.PLACE_INFO,
                    PlaceInfo::class.java
                )
            }
            placeInfo?.let {
                addPinViewModel.setPinState(it.addressName, it.lat, it.lng)
            }
        }

    LaunchedEffect(key1 = Unit) {
        addPinViewModel.regPinEvent.collect { event ->
            when (event) {
                is RegPinEvent.Error -> {
                    regPinErrorMessage =
                        ContextCompat.getString(context, R.string.reg_pin_error_message)
                    showRegPinErrorDialog = true
                }

                RegPinEvent.Success -> {
                    context.removeTempImagesDir()
                    showRegPinSuccessDialog = true
                }

                RegPinEvent.EmptyAddress -> {
                    regPinErrorMessage =
                        ContextCompat.getString(context, R.string.check_pin_error_message)
                    showRegPinErrorDialog = true
                }

                RegPinEvent.EmptyImage -> {
                    regPinErrorMessage =
                        ContextCompat.getString(context, R.string.add_image_message)
                    showRegPinErrorDialog = true
                }

                RegPinEvent.EmptyContent -> {
                    regPinErrorMessage =
                        ContextCompat.getString(context, R.string.write_content_message)
                    showRegPinErrorDialog = true
                }
            }
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            FileUtil.deleteTempDirectory(context)
        }
    }

    if (showRegPinErrorDialog) {
        AlertDialog(
            onDismissRequest = { showRegPinErrorDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRegPinErrorDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.ok)
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(id = R.string.default_dialog_title)
                )
            },
            text = {
                Text(
                    text = regPinErrorMessage
                )
            }
        )
    }

    if (showRegPinSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showRegPinSuccessDialog = false
                onBackPressedDispatcher?.onBackPressed()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRegPinSuccessDialog = false
                        onBackPressedDispatcher?.onBackPressed()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.ok)
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(id = R.string.reg_pin_success_title)
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.reg_pin_success_message)
                )
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AddPinScreen(
            onBack = {
                onBackPressedDispatcher?.onBackPressed()
            },
            onClickDeleteSelectedImage = addPinViewModel::removeImage,
            onClickRegPin = { content ->
                addPinViewModel.regPin(content)
            },
            onSelectedImages = addPinViewModel::addImage,
            selectedImageList = selectedImageList,
            onClickAddressSearch = {
                searchPlaceLauncher.launch(Intent(context, SearchPlaceActivity::class.java))
            },
            pinAddress = pinState?.address ?: "",
            onShowSnackbar = {
                scope.launch {
                    onShowSnackbar(it, null)
                }
            }
        )
        if (addPinViewModel.isLoading) {
            Box(
                modifier = Modifier
                    .pointerInteropFilter { addPinViewModel.isLoading }
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPinScreen(
    onBack: () -> Unit,
    onClickDeleteSelectedImage: (index: Int) -> Unit,
    onClickRegPin: (content: String) -> Unit,
    onSelectedImages: (image: List<Uri>) -> Unit,
    selectedImageList: List<Uri>,
    onClickAddressSearch: () -> Unit,
    pinAddress: String,
    onShowSnackbar: (String) -> Unit
) {
    var showImageAddMenuDialog by remember { mutableStateOf(false) }
    var content by remember { mutableStateOf("") }

    if (showImageAddMenuDialog) {
        ImageAddMenuDialog(
            onDismissRequest = {
                showImageAddMenuDialog = false
            },
            onSelectedImages = {
                onSelectedImages(it)
                showImageAddMenuDialog = false
            },
            onError = { e ->
                logcat("AddPinScreen", LogPriority.ERROR) { e.asLog() }
                showImageAddMenuDialog = false
                e.message?.let {
                    onShowSnackbar(it)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.new_post)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = stringResource(
                                id = R.string.close
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onClickRegPin(content)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_done),
                            contentDescription = stringResource(
                                id = R.string.reg_pin
                            )
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .fillMaxSize()
        ) {
            ImageLazyRow(
                onClickImportImage = {
                    showImageAddMenuDialog = true
                },
                selectedImageList = selectedImageList,
                onClickDeleteSelectedImage = onClickDeleteSelectedImage
            )
            ElevatedButton(
                onClick = onClickAddressSearch,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(text = stringResource(id = R.string.search_address))
            }
            Text(
                text = stringResource(id = R.string.pin_location_for_address, pinAddress),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .fillMaxWidth(),
            )
            TextField(
                value = content,
                onValueChange = {
                    content = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = EveryPinTheme.typography.titleMedium,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.hint_content),
                        style = EveryPinTheme.typography.titleMedium.copy(
                            color = Color.Gray
                        )
                    )
                },
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageLazyRow(
    onClickImportImage: () -> Unit,
    selectedImageList: List<Uri>,
    onClickDeleteSelectedImage: (index: Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 5.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilledIconButton(
                onClick = onClickImportImage,
                modifier = Modifier.size(90.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_a_photo),
                    contentDescription = null
                )
            }
        }
        itemsIndexed(selectedImageList) { index, uri ->
            Box {
                CommonAsyncImage(
                    data = uri,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
                    LocalRippleConfiguration provides RippleConfiguration(
                        rippleAlpha = RippleAlpha(
                            0f,
                            0f,
                            0f,
                            0f
                        )
                    )
                ) {
                    IconButton(
                        onClick = {
                            onClickDeleteSelectedImage(index)
                        },
                        modifier = Modifier
                            .padding(3.dp)
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cancel),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPinScreenPreview() {
    EveryPinTheme {
        AddPinScreen(
            onBack = {},
            onClickDeleteSelectedImage = {},
            onClickRegPin = {},
            onSelectedImages = {},
            selectedImageList = emptyList(),
            onClickAddressSearch = {},
            pinAddress = "서울특별시 중구 세종대로 110",
            onShowSnackbar = {}
        )
    }
}