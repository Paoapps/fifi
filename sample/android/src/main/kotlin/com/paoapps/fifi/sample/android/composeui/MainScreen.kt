package com.paoapps.fifi.sample.android.composeui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController


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
