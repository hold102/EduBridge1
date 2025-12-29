package com.example.edubridge.utils;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

public class LanguageHelper {

    public static void setLanguage(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        context.getResources().updateConfiguration(
                config,
                context.getResources().getDisplayMetrics()
        );
    }
}
