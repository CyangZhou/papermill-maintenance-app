package com.papermill.maintenance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.papermill.maintenance.ui.navigation.Screen
import com.papermill.maintenance.ui.screen.RecordDetailScreen
import com.papermill.maintenance.ui.screen.RecordListScreen
import com.papermill.maintenance.ui.theme.MaintenanceRecorderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaintenanceRecorderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.RecordList.route
                    ) {
                        composable(Screen.RecordList.route) {
                            RecordListScreen(
                                onAddRecord = {
                                    navController.navigate(Screen.AddRecord.route)
                                },
                                onRecordClick = { recordId ->
                                    navController.navigate(Screen.RecordDetail.createRoute(recordId))
                                }
                            )
                        }
                        
                        composable(
                            route = Screen.RecordDetail.route,
                            arguments = listOf(
                                navArgument("recordId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L
                            RecordDetailScreen(
                                recordId = recordId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable(Screen.AddRecord.route) {
                            RecordDetailScreen(
                                recordId = 0L,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
