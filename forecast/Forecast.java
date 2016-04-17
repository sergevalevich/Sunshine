package com.valevich.sunshine.forecast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NotePad.by on 17.04.2016.
 */
public class Forecast {
    private List<DayInfo> list = new ArrayList<>();

    public List<DayInfo> getDayInfo() {
        return list;
    }
}

