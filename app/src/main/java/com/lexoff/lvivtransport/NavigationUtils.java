package com.lexoff.lvivtransport;

import android.app.Activity;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class NavigationUtils {

    public static void openMapFragment(Activity activity){
        FragmentManager fragmentManager = ((MainActivity) activity).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        MapFragment mapFragment = MapFragment.newInstance();

        fragmentTransaction.add(R.id.fragment_container, mapFragment);
        fragmentTransaction.commit();
    }

}
