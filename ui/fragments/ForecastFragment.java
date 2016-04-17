package com.valevich.sunshine.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.valevich.sunshine.R;
import com.valevich.sunshine.forecast.DayInfo;
import com.valevich.sunshine.forecast.Forecast;
import com.valevich.sunshine.ui.activities.DetailActivity;
import com.valevich.sunshine.ui.dialogs.AlertDialogFragment;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by NotePad.by on 14.04.2016.
 */
public class ForecastFragment extends Fragment {

    private static final String TAG = ForecastFragment.class.getSimpleName();
    private OkHttpClient mOkHttpClient;
    private ArrayAdapter<String> mListAdapter;
    @Bind(R.id.listview_forecast)
    ListView mWeatherList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mOkHttpClient = new OkHttpClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        setupListView();

        getWeather("Minsk"); //new thread starts

        return rootView;    //background thread still running after return
    }

    private void getWeather(String city) {
        String format = "json";
        String units = "metric";
        String numDays = "7";
        String key = getResources().getString(R.string.weather_key);
        final String BASEURL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";
        final String KEY_PARAM = "APPID";
        Uri builtUri = Uri.parse(BASEURL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, city)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, numDays)
                .appendQueryParameter(KEY_PARAM, key).build();

        Log.d(TAG,builtUri.toString());


        final Request request = new Request.Builder()
                .url(builtUri.toString())
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) { //NOT A UI THREAD
                alertUserAboutError();
            }

            @Override
            public void onResponse(final Response response) throws IOException { // NOT A UI THREAD
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body().string();
                        final List<String> weatherData = getWeatherDataFromJson(responseString);//only after parsing we run on ui thread //action placed in the queue/ in stormy 2 method calls in order
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateWeatherListContent(weatherData);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        alertUserAboutError();
                    }
                } else {
                    alertUserAboutError();
                }
            }
        });
    }


    private void updateWeatherListContent(List<String> weatherData) {

        if (weatherData != null) {
            mListAdapter.clear();
            mListAdapter.addAll(weatherData);
        } else {
            alertUserAboutError();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                getWeather("Moscow");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getActivity().getFragmentManager(), "error_dialog");
    }


    private List<String> getWeatherDataFromJson(String forecastJsonStr)
            throws JSONException {

        Gson gson = new Gson();
        Forecast forecast = gson.fromJson(forecastJsonStr,Forecast.class);

        List<String> weatherData = new ArrayList<>();

        for(DayInfo dayInfo:forecast.getDayInfo()) {
            String day = dayInfo.getReadableDate();
            String description = dayInfo.getWeather().get(0).getDescription();
            String highLow = dayInfo.getTemp().getFormattedHighLows();
            String dayData = day + " " + description + " " + highLow;
            weatherData.add(dayData);
        }

        return weatherData;
    }

    private void setupListView() {
        String[] daysArray = {"Monday"
                , "Tuesday"
                , "Wednesday"
                , "Thursday"
                , "Friday"
                , "Saturday"
                , "Sunday"
        };
        List<String> days = new ArrayList<>(Arrays.asList(daysArray));
        mListAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textView,
                days);
        mWeatherList.setAdapter(mListAdapter);
        mWeatherList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(),mListAdapter.getItem(position),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getContext(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT,mListAdapter.getItem(position));
                startActivity(intent);
            }
        });
    }
}
