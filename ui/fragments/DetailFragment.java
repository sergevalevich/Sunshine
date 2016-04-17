package com.valevich.sunshine.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.valevich.sunshine.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DetailFragment extends Fragment {

    @Bind(R.id.detailText)
    TextView mTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this,rootView);

        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String dailyInfo = intent.getStringExtra(Intent.EXTRA_TEXT);
            mTextView.setText(dailyInfo);
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        switch (id) {
//            case R.id.action_refresh:
//                return true;
//            default:
//
//        }
        return super.onOptionsItemSelected(item);
    }

}
