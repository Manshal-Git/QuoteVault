package com.example.quotevault.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.quotevault.navigation.BottomNavItem
import com.example.quotevault.navigation.bottomNavItems
import com.example.quotevault.ui.favourites.FavouritesScreen
import com.example.quotevault.ui.profile.AccountDataScreen
import com.example.quotevault.ui.profile.AppearanceScreen
import com.example.quotevault.ui.profile.ProfileScreen
import com.example.quotevault.ui.quotes.QuotesDiscoveryScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val shouldShowBottomBar = currentDestination?.route in bottomNavItems.map { it.route }
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { 
                            it.route == item.route 
                        } == true
                    
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) {
                QuotesDiscoveryScreen()
            }
            
            composable(BottomNavItem.Favourites.route) {
                FavouritesScreen()
            }
            
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onNavigateToPersonalization = {
                        navController.navigate("personalization")
                    },
                    onNavigateToAccountData = {
                        navController.navigate("account_data")
                    }
                )
            }
            
            composable("personalization") {
                AppearanceScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("account_data") {
                AccountDataScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
