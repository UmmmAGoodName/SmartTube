package com.liskovsoft.smartyoutubetv2.tv.ui.compose.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

// Track B / Phase C0 toolchain spike: proves a Compose-for-TV screen can render and take D-pad
// focus in this app before any real Leanback screen is migrated. Debug-build only, launched
// directly from the TV home screen (see src/debug/AndroidManifest.xml) - not reachable from a
// release APK and not wired into the app's own navigation.
class ComposeSpikeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        var presses by remember { mutableIntStateOf(0) }

                        Text(text = "Compose for TV toolchain spike (Phase C0)")
                        Text(text = "D-pad focus + click presses: $presses")
                        Button(onClick = { presses++ }) {
                            Text(text = "Press me with the remote")
                        }
                    }
                }
            }
        }
    }
}
