package com.valevich.sunshine.forecast;

import android.content.Context;

/**
 * Created by NotePad.by on 17.04.2016.
 */
public class DayWeather {

    private final String UNIT_TYPE_METRIC = "METRIC";
    private final String UNIT_TYPE_IMPERIAL = "IMPERIAL";

    private double min;

    private double max;


    public String getFormattedHighLowsByUnits(String units) {

        if(units.equals(UNIT_TYPE_IMPERIAL)) {
            min = (1.8) * min + 32;
            max = (1.8) * max + 32;
        } else if(!units.equals(UNIT_TYPE_METRIC)) {
            return units + " type not found";
        }

        long roundedHigh = Math.round(max);
        long roundedLow = Math.round(min);

        return roundedHigh + "/" + roundedLow;
    }
}
