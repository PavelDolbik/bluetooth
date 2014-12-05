package com.itransition.android.blthnfc.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.itransition.android.blthnfc.R;
import com.skyfishjy.library.RippleBackground;

/**
 * Created by p.dolbik on 25.11.2014.
 */
public class NFCFragment extends android.support.v4.app.Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nfc, container, false);

        return view;
    }
}
