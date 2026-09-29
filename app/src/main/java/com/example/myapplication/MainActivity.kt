package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    remember { Economy.load(context); 0 }
    val storage = remember { AppStorage(context) }

    var tutorialActive by remember { mutableStateOf(storage.tutorialActive) }

    var tutorialCanSkip by remember { mutableStateOf(false) }

    var screen by remember {
        mutableStateOf(
            when {
                !storage.isPetCreated -> "welcome"
                tutorialActive -> "tutorial"
                else -> "main"
            }
        )
    }

    val goHome: () -> Unit = {
        screen = if (tutorialActive) "tutorial" else "main"
    }

    when (screen) {
        "welcome" -> WelcomeScreen(onStart = { screen = "pet_setup" })

        "pet_setup" -> PetSetupScreen(storage) { screen = "color_setup" }

        "color_setup" -> ColorSetupScreen(
            storage = storage,
            onDone = {
                if (storage.tutorialShown) {
                    screen = "main"
                } else {
                    storage.tutorialActive = true
                    tutorialActive = true
                    tutorialCanSkip = false
                    screen = "tutorial"
                }
            },
            onBack = { screen = "pet_setup" }
        )

        "tutorial" -> MainScreen(
            storage = storage,
            onSettingsClick = { screen = "settings" },
            onShopClick = { screen = "shop" },
            onCoinsClick = { screen = "coins" },
            onPiggyClick = { screen = "piggy" },
            onTasksClick = { screen = "tasks" },
            onGoalClick = { screen = "goal" },
            onTelescopeClick = { screen = "telescope" },
            onConsoleGameClick = { screen = "console_game" },
            showTutorial = true,
            canSkipTutorial = tutorialCanSkip,
            onTutorialFinished = {
                storage.tutorialShown = true
                storage.tutorialActive = false
                storage.tutorialStep = 0
                tutorialActive = false
                tutorialCanSkip = false
                screen = "main"
            }
        )

        "main" -> MainScreen(
            storage = storage,
            onSettingsClick = { screen = "settings" },
            onShopClick = { screen = "shop" },
            onCoinsClick = { screen = "coins" },
            onPiggyClick = { screen = "piggy" },
            onTasksClick = { screen = "tasks" },
            onGoalClick = { screen = "goal" },
            onTelescopeClick = { screen = "telescope" },
            onConsoleGameClick = { screen = "console_game" },
            showTutorial = false
        )

        "settings" -> SettingsScreen(
            storage = storage,
            onBack = { screen = "main" },
            onPetDeleted = {
                tutorialActive = storage.tutorialActive
                tutorialCanSkip = false
                screen = "welcome"
            },
            onRepeatTutorial = {
                storage.tutorialStep = 0
                storage.tutorialActive = true
                tutorialActive = true
                tutorialCanSkip = true
                screen = "tutorial"
            },
            onOpenDev = { screen = "dev" },
            onOpenParent = { screen = "parent" }
        )

        "telescope" -> TelescopeScreen(
            onBack = { screen = "main" }
        )

        "console_game" -> ConsoleGameScreen(
            storage = storage,
            onBack = { screen = "main" }
        )

        "dev" -> DevScreen(
            storage = storage,
            onBack = { screen = "settings" }
        )

        "parent" -> ParentScreen(
            storage = storage,
            onBack = { screen = "settings" },
            onOpenDev = { screen = "dev" }
        )

        "shop" -> ShopScreen(
            storage = storage,
            onBack = goHome,
            onCartClick = { screen = "cart" }
        )
        "cart" -> CartScreen(
            storage = storage,
            onBack = { screen = "shop" }
        )
        "coins" -> CoinsScreen(storage = storage, onBack = goHome)
        "piggy" -> PiggyScreen(
            storage = storage,
            onBack = goHome,
            onRestart = {
                storage.reset()
                tutorialActive = false
                tutorialCanSkip = false
                screen = "welcome"
            }
        )
        "tasks" -> TasksScreen(storage = storage, onBack = goHome)
        "goal" -> GoalScreen(storage = storage, onBack = goHome)
    }
}