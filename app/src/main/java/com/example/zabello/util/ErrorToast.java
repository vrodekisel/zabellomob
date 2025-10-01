package com.example.zabello.util;

import android.content.Context;
import android.widget.Toast;

import com.example.zabello.R;

import java.io.IOException;
import java.net.SocketTimeoutException;

public final class ErrorToast {
    private ErrorToast(){}

    public static void show(Context ctx, Throwable t) {
        String msg;
        if (t instanceof SocketTimeoutException) {
            msg = getString(ctx, R.string.error_title);
        } else if (t instanceof IOException) {
            msg = getString(ctx, R.string.error_message);
        } else {
            msg = getString(ctx, R.string.btn_retry);
        }
        Toast.makeText(ctx.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private static String getString(Context ctx, int resId) {
        return ctx.getString(resId);
    }

    public static void showNoInternet(Context ctx) {
        Toast.makeText(ctx.getApplicationContext(), getString(ctx, R.string.btn_cancel), Toast.LENGTH_SHORT).show();
    }
}
