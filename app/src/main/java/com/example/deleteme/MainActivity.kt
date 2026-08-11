//package com.example.deleteme
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import com.example.deleteme.ui.DeleteMeScreen
//import com.example.deleteme.ui.theme.DeleteMeTheme
//
//class MainActivity : ComponentActivity() {
//
//    private var cameraPermissionGranted by mutableStateOf(false)
//    private var cameraPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
//            cameraPermissionGranted = granted
//        }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            DeleteMeTheme {
//                DeleteMeScreen(applicationContext, cameraPermissionLauncher)
//            }
//        }
//    }
//}

package com.example.deleteme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.deleteme.ui.DeleteMeScreen
import com.example.deleteme.ui.theme.DeleteMeTheme

class MainActivity : ComponentActivity() {
    private var cameraPermissionGranted by mutableStateOf(false)
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                cameraPermissionGranted = granted
            }
        setContent {
            DeleteMeTheme {
                DeleteMeScreen(
                    cameraPermissionGranted = cameraPermissionGranted,
                    onRequestCameraPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            }
        }
    }
}