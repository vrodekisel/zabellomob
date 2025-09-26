package com.example.zabello.smoke;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.zabello.activities.RegisterActivity; // LAUNCHER у тебя был RegisterActivity

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppSmokeTest {

    @Rule
    public ActivityScenarioRule<RegisterActivity> rule =
            new ActivityScenarioRule<>(RegisterActivity.class);

    @Test
    public void app_launches_without_crash() {
        // Если активити поднялась — тест зелёный. Доп. ассерты не обязательны.
        // Цель — «верификация прототипа» по методичке.
    }
}
