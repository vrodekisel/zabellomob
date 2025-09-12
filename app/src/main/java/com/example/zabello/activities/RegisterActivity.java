package com.example.zabello.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zabello.R;
import com.example.zabello.data.entity.User;
import com.example.zabello.repository.HealthRepository;
import com.example.zabello.utils.ValidationLogic;

public class RegisterActivity extends AppCompatActivity {

    private HealthRepository repo;

    private EditText etLogin;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private TextView tvModeTitle;
    private Button btnRegister;
    private Button btnLogin;

    private boolean isLoginMode = false; // false = Регистрация (по умолчанию), true = Вход

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        repo = new HealthRepository(getApplicationContext());

        tvModeTitle = findViewById(R.id.tvModeTitle);
        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);

        // Стартуем в режиме Регистрации
        applyMode(false);

        btnRegister.setOnClickListener(v -> {
            if (isLoginMode) {
                // если мы в режиме Входа, по нажатию «Зарегистрироваться» переключаемся в Регистрацию
                applyMode(false);
                return;
            }
            performRegister();
        });

        btnLogin.setOnClickListener(v -> {
            if (!isLoginMode) {
                // если мы в режиме Регистрации, по нажатию «Войти» переключаемся во Вход
                applyMode(true);
                return;
            }
            performLogin();
        });
    }

    private void applyMode(boolean loginMode) {
        isLoginMode = loginMode;
        tvModeTitle.setText(loginMode ? "Вход" : "Регистрация");
        etPasswordConfirm.setVisibility(loginMode ? android.view.View.GONE : android.view.View.VISIBLE);
        etPasswordConfirm.setText("");
        // Подсказки на кнопках остаются постоянными: «Зарегистрироваться» и «Войти»
    }

    private void performRegister() {
        String login = etLogin.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String pass2 = etPasswordConfirm.getText().toString();

        if (!ValidationLogic.isValidLogin(login)) {
            etLogin.setError("Минимум 3 символа");
            return;
        }
        if (!ValidationLogic.isValidPassword(pass)) {
            etPassword.setError("Минимум 6 символов");
            return;
        }
        if (!pass.equals(pass2)) {
            etPasswordConfirm.setError("Пароли не совпадают");
            return;
        }

        repo.isLoginTaken(login, taken -> runOnUiThread(() -> {
            if (taken) {
                etLogin.setError("Логин уже занят");
            } else {
                User u = new User();
                u.login = login;
                u.passwordHash = ValidationLogic.sha256(pass);
                u.fullName = "";

                repo.insertUser(u, id -> runOnUiThread(() -> {
                    if (id > 0) {
                        Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                        openMainAndFinish();
                    } else {
                        Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                    }
                }));
            }
        }));
    }

    private void performLogin() {
        String login = etLogin.getText().toString().trim();
        String pass = etPassword.getText().toString();

        if (!ValidationLogic.isValidLogin(login)) {
            etLogin.setError("Укажите логин");
            return;
        }
        if (!ValidationLogic.isValidPassword(pass)) {
            etPassword.setError("Слишком короткий пароль");
            return;
        }

        String hash = ValidationLogic.sha256(pass);
        repo.signIn(login, hash, user -> runOnUiThread(() -> {
            if (user != null) {
                Toast.makeText(this, "Добро пожаловать", Toast.LENGTH_SHORT).show();
                openMainAndFinish();
            } else {
                Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void openMainAndFinish() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
