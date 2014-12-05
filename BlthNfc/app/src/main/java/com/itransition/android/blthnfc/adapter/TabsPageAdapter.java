package com.itransition.android.blthnfc.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.itransition.android.blthnfc.fragment.BluetoothFragment;
import com.itransition.android.blthnfc.fragment.NFCFragment;

/**
 * Created by p.dolbik on 25.11.2014.
 */
public class TabsPageAdapter extends FragmentPagerAdapter {

    public TabsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    private String[] tabNames = {"Bluetooth"};


    @Override
    public Fragment getItem(int i) {

        switch ( i ) {
            case 0:
                return new BluetoothFragment();
            case 1:
                return new NFCFragment();
        }

        return null;
    }


    @Override
    public int getCount() {
        return tabNames.length;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return tabNames[position];
    }
}
