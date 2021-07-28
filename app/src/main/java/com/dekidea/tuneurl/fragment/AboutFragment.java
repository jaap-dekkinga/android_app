package com.dekidea.tuneurl.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.util.Constants;

public class AboutFragment extends Fragment implements Constants {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_about_page, container, false);

        return rootView;
    }
}
