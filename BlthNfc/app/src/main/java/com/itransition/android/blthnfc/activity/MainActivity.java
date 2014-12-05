package com.itransition.android.blthnfc.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.itransition.android.blthnfc.R;
import com.itransition.android.blthnfc.adapter.TabsPageAdapter;


public class MainActivity extends FragmentActivity  {

    private ViewPager viewPager;
    private TabsPageAdapter tabsPageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabsPageAdapter = new TabsPageAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(tabsPageAdapter);

    }


    public void registerReceiver(BroadcastReceiver bluetoothState, String actionStateChanging) {
    }
}
