package com.valevich.sunshine.forecast;

/**
 * Created by NotePad.by on 17.04.2016.
 */
public class DayWeather {
    private double min;

    private double max;

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public String getFormattedHighLows() {
        long roundedHigh = Math.round(max);
        long roundedLow = Math.round(min);
        return roundedHigh + "/" + roundedLow;
    }
}
