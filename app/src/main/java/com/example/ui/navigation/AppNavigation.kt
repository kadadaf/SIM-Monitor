package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.SIMMonitorApp
import com.example.ui.components.BottomNavigationBar
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.simdetail.SIMDetailScreen
import com.example.ui.simdetail.SIMDetailViewModel
import com.example.ui.addsim.AddEditSIMScreen
import com.example.ui.addsim.AddEditSIMViewModel
import com.example.ui.rules.RulesScreen
import com.example.ui.rules.RulesViewModel
import com.example.ui.rules.AddEditRuleScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.history.HistoryViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.search.SearchScreen
import com.example.ui.search.SearchViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    val context = LocalContext.current
    val app = context.applicationContext as SIMMonitorApp
    val factory = remember { ViewModelFactory(app) }

    val settingsState by app.settingsDataStore.settingsFlow.collectAsStateWithLifecycle(
        initialValue = com.example.settings.AppSettings()
    )

    // Bottom Navigation visible routes list
    val bottomTabRoutes = listOf("dashboard", "rules", "history", "settings")
    val shouldShowBottomBar = currentRoute in bottomTabRoutes

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    currentDestination = currentRoute,
                    language = settingsState.language,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Avoid restoration issues by requesting fresh trees
                            restoreState = false
                        }
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = modifier.padding(innerPadding)
        ) {
            // 1. Dashboard Tab
            composable("dashboard") {
                val dbViewModel: DashboardViewModel = viewModel(factory = factory)
                DashboardScreen(
                    viewModel = dbViewModel,
                    onAddSimClick = { navController.navigate("add_sim") },
                    onCardClick = { simId -> navController.navigate("sim_detail/$simId") },
                    onSearchClick = { navController.navigate("search") },
                    onViewAllClick = { filter ->
                        when (filter) {
                            "settings" -> navController.navigate("settings")
                            else -> navController.navigate("history")
                        }
                    }
                )
            }

            // 2. Rules Tab
            composable("rules") {
                val rulesViewModel: RulesViewModel = viewModel(factory = factory)
                RulesScreen(
                    viewModel = rulesViewModel,
                    onAddRuleClick = { navController.navigate("add_rule") },
                    onEditRuleClick = { ruleId -> navController.navigate("edit_rule/$ruleId") }
                )
            }

            // 3. History Tab
            composable("history") {
                val historyViewModel: HistoryViewModel = viewModel(factory = factory)
                // When we display history screen, refresh state
                HistoryScreen(viewModel = historyViewModel)
            }

            // 4. Settings Tab
            composable("settings") {
                val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    }
                )
            }

            // 5. Add SIM full screen sub page
            composable("add_sim") {
                val addSIMViewModel: AddEditSIMViewModel = viewModel(factory = factory)
                AddEditSIMScreen(
                    viewModel = addSIMViewModel,
                    simId = null,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 6. Edit SIM sub page
            composable(
                route = "edit_sim/{simId}",
                arguments = listOf(navArgument("simId") { type = NavType.IntType })
            ) { backStackEntry ->
                val simId = backStackEntry.arguments?.getInt("simId")
                val editSIMViewModel: AddEditSIMViewModel = viewModel(factory = factory)
                AddEditSIMScreen(
                    viewModel = editSIMViewModel,
                    simId = simId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 7. SIM Details Sub Page
            composable(
                route = "sim_detail/{simId}",
                arguments = listOf(navArgument("simId") { type = NavType.IntType })
            ) { backStackEntry ->
                val simId = backStackEntry.arguments?.getInt("simId") ?: 0
                val detailViewModel: SIMDetailViewModel = viewModel(factory = factory)
                SIMDetailScreen(
                    viewModel = detailViewModel,
                    simId = simId,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("edit_sim/$id") }
                )
            }

            // 8. Custom rule creation form
            composable("add_rule") {
                val rulesViewModel: RulesViewModel = viewModel(factory = factory)
                AddEditRuleScreen(
                    viewModel = rulesViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 9. Search Bar Page
            composable("search") {
                val searchViewModel: SearchViewModel = viewModel(factory = factory)
                SearchScreen(
                    viewModel = searchViewModel,
                    onBackClick = { navController.popBackStack() },
                    onCardClick = { simId -> navController.navigate("sim_detail/$simId") }
                )
            }

            // 10. Edit Custom Rule Page
            composable(
                route = "edit_rule/{ruleId}",
                arguments = listOf(navArgument("ruleId") { type = NavType.IntType })
            ) { backStackEntry ->
                val ruleId = backStackEntry.arguments?.getInt("ruleId")
                val rulesViewModel: RulesViewModel = viewModel(factory = factory)
                AddEditRuleScreen(
                    viewModel = rulesViewModel,
                    ruleId = ruleId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
