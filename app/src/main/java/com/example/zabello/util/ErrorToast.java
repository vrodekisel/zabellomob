package com.example.zabello.util;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.net.SocketTimeoutException;

public final class ErrorToast {
    private ErrorToast(){}

    public static void show(Context ctx, Throwable t) {
        String msg;
        if (t instanceof SocketTimeoutException) msg = "Тайм-аут сети. Повторите попытку.";
        else if (t instanceof IOException)       msg = "Нет интернета или ошибка сети.";
        else                                     msg = "Произошла ошибка. Попробуйте ещё раз.";
        Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void showNoInternet(Context ctx) {
        Toast.makeText(ctx.getApplicationContext(), "Нет подключения к интернету.", Toast.LENGTH_SHORT).show();
    }
}
