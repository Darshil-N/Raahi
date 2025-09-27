package com.example.raahi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.raahi.NavRoutes // Import the centralized NavRoutes
import com.example.raahi.ui.theme.RaahiTheme // Import the theme
import com.example.raahi.ui.viewmodels.MoreViewModel

// Data class structure remains the same
data class MoreScreenItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

// Updated items list to use centralized NavRoutes
val moreScreenItems = listOf(
    MoreScreenItem(NavRoutes.BOOKING_SCREEN, "My Bookings", Icons.Filled.EventSeat),
    MoreScreenItem(NavRoutes.LOCAL_TRANSPORT_SCREEN, "Book Local Transport", Icons.Filled.Commute),
    MoreScreenItem(NavRoutes.PERSONAL_DETAILS_SCREEN, "Personal Details", Icons.Filled.Person),
    MoreScreenItem(NavRoutes.MEDICAL_DETAILS_SCREEN, "Medical Details", Icons.Filled.MedicalServices),
    MoreScreenItem(NavRoutes.SCORE_HISTORY_SCREEN, "Safety Score & History", Icons.Filled.Shield),
    MoreScreenItem(NavRoutes.VERIFICATION_SCREEN, "Verification", Icons.Filled.VerifiedUser),
    MoreScreenItem("logout", "Logout", Icons.Filled.Logout) // "logout" is a special case
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    appNavController: NavController? = null,
    moreViewModel: MoreViewModel = viewModel()
) {
    val uiState by moreViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "More Options", 
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 8.dp)
        ) {
            items(moreScreenItems) { item ->
                ListItem(
                    headlineContent = {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingContent = {
                        Icon(
                            item.icon,
                            contentDescription = item.title
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clickable {
                            if (item.route == "logout") {
                                moreViewModel.logout()
                                // Actual navigation to login screen should be handled by observing a state from ViewModel
                                // e.g., appNavController?.navigate(NavRoutes.LOGIN_SCREEN) { popUpTo(NavRoutes.APP_MAIN) { inclusive = true } }
                            } else {
                                appNavController?.navigate(item.route)
                            }
                        },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        headlineColor = MaterialTheme.colorScheme.onSurface,
                        leadingIconColor = MaterialTheme.colorScheme.primary
                    )
                )
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MoreScreenPreview() {
    RaahiTheme {
        MoreScreen(appNavController = rememberNavController())
    }
}
