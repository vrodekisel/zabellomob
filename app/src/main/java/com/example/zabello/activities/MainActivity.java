package com.example.zabello.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.zabello.R;
import com.example.zabello.domain.alerts.NotificationHelper;
import com.example.zabello.domain.alerts.NotificationScheduler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // --- Toolbar (без NPE) ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // Можно установить дефолтный заголовок, если нужно:
            // getSupportActionBar() может быть null, поэтому проверяем:
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.app_name);
            }
        }

        // --- NavHost + NavController ---
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // --- AppBarConfiguration: задаём top-level пункты (id из графа навигации) ---
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dashboardFragment,
                R.id.referenceFragment,
                R.id.statsFragment,
                R.id.profileFragment
        ).build();

        // Связываем тулбар с навконтроллером (если тулбар есть)
        if (toolbar != null && navController != null) {
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        }

        // --- BottomNavigationView (если он есть в activity_main) ---
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null && navController != null) {
            NavigationUI.setupWithNavController(bottomNav, navController);
        }

        // --- Канал уведомлений + периодические проверки аномалий ---
        // Канал уведомлений (на случай, если Application ещё не создавал)
        NotificationHelper.createChannels(this);
        // Поставим (или обновим) периодические проверки аномалий.
        // Идемпотентно: внутри используется enqueueUniquePeriodicWork.
        NotificationScheduler.schedulePeriodicChecks(getApplicationContext());

        // --- Правильная обработка системной "назад" вместо deprecated onBackPressed() ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (navController != null && navController.popBackStack()) {
                    // вернулись по стеку фрагментов
                    return;
                }
                // иначе — дефолтное поведение (закрыть активити)
                setEnabled(false);
                MainActivity.super.onBackPressed();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // корректный Up (стрелка в тулбаре) с учётом AppBarConfiguration
        return navController != null
                && (NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp());
    }
}
