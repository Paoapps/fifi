package com.paoapps.fifi.sample.android.composeui

import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.paoapps.fifi.sample.android.composeui.detail.CoffeeDetailView
import com.paoapps.fifi.sample.android.composeui.home.HomeView

@Composable
fun NavigationGraph(
    modifier: Modifier,
    navController: NavHostController,
) {
    NavHost(
        navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable(
            "home",
        ) {
            HomeView { id ->
                navController.navigate("home/coffee/$id")
            }
        }

        composable(
            "home/coffee/{id}",
        ) {
            it.arguments?.getString("id")?.let { id ->
                CoffeeDetailView(
                    id = id.toInt(),
                )
            }
        }
    }
}
