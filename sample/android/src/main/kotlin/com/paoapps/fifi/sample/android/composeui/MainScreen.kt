package com.paoapps.fifi.sample.android.composeui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.paoapps.fifi.sample.android.composeui.home.HomeView


@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        NavigationGraph(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
        )
    }
}
