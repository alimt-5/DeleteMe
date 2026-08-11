//package com.example.deleteme.ui
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.Context
//import android.content.pm.PackageManager
//import androidx.activity.result.ActivityResultLauncher
//import androidx.compose.runtime.Composable
//import androidx.core.content.ContextCompat
//
//@SuppressLint("UnrememberedMutableState")
//@Composable
//fun DeleteMeScreen(context: Context, cameraPermissionLauncher: ActivityResultLauncher<String>) {
//    BackgroundCaptureScreen(
//        cameraPermissionGranted = ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.CAMERA
//        ) == PackageManager.PERMISSION_GRANTED,
//        onRequestCameraPermission = {
//            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
//        }
//    )
//}

package com.example.deleteme.ui

import androidx.compose.runtime.Composable

@Composable
fun DeleteMeScreen(
    cameraPermissionGranted: Boolean,
    onRequestCameraPermission: () -> Unit
) {
    BackgroundCaptureScreen(
        cameraPermissionGranted = cameraPermissionGranted,
        onRequestCameraPermission = onRequestCameraPermission
    )
}