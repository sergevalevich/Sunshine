package com.valevich.sunshine.forecast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by NotePad.by on 17.04.2016.
 */
public class DayInfo {
    private long dt;
    private DayWeather temp;
    private List<WeatherDescription> weather = new ArrayList<>();

    public long getDate() {
        return dt;
    }

    public DayWeather getTemp() {
        return temp;
    }

    public List<WeatherDescription> getWeather() {
        return weather;
    }

    public String getReadableDate() {
        Date date = new Date(dt*1000);
        SimpleDateFormat formatter = new SimpleDateFormat("E, MMM, d", Locale.getDefault());
        return formatter.format(date);
    }
}
