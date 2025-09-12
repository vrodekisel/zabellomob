package com.example.zabello.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.zabello.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 1) Toolbar как ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2) Достаём NavController из NavHostFragment (надёжнее, чем findNavController в Activity)
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment not found. Check activity_main.xml (id=nav_host_fragment).");
        }
        navController = navHostFragment.getNavController();

        // 3) Верхнеуровневые пункты = вкладки из нижней навигации (чтобы не показывать стрелку «назад» на них)
        Set<Integer> topLevel = new HashSet<>(Arrays.asList(
                R.id.dashboardFragment,
                R.id.referenceFragment,
                R.id.statsFragment,
                R.id.profileFragment
        ));
        appBarConfig = new AppBarConfiguration.Builder(topLevel).build();

        // 4) Связываем ActionBar с NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);

        // 5) Привязываем BottomNavigationView к NavController
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // (опционально) Отключаем повторную навигацию при повторном тапе по текущей вкладке
        bottomNav.setOnItemReselectedListener(item -> {
            // no-op: не делаем pop/перенавигацию
        });

        // (опционально) Обновляем заголовок тулбара по label из графа
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            CharSequence label = destination.getLabel();
            if (label != null) {
                getSupportActionBar().setTitle(label);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && (NavigationUI.navigateUp(navController, appBarConfig) || super.onSupportNavigateUp());
    }

    @Override
    public void onBackPressed() {
        // Правильный back: если можем вернуться по стеку навигации — возвращаемся,
        // иначе — стандартное поведение (закрыть активити).
        if (navController != null && navController.popBackStack()) {
            return;
        }
        super.onBackPressed();
    }
}
