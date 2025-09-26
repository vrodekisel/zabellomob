package com.example.zabello.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** Хранение последних фильтров/поиска. Простая реализация на SharedPreferences. */
public class FilterPrefs {

    private static final String FILE = "filters";
    private static final String K_STATS_TYPE = "stats.typeId";
    private static final String K_STATS_DAYS = "stats.days";
    private static final String K_REF_QUERY  = "ref.query";

    private final SharedPreferences sp;

    public FilterPrefs(Context ctx) {
        this.sp = ctx.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public long getStatsTypeId() { return sp.getLong(K_STATS_TYPE, -1L); }
    public void setStatsTypeId(long id) { sp.edit().putLong(K_STATS_TYPE, id).apply(); }

    public int getStatsDays() { return sp.getInt(K_STATS_DAYS, 7); }
    public void setStatsDays(int days) { sp.edit().putInt(K_STATS_DAYS, days).apply(); }

    public String getRefQuery() { return sp.getString(K_REF_QUERY, ""); }
    public void setRefQuery(String q) { sp.edit().putString(K_REF_QUERY, q != null ? q : "").apply(); }
}
