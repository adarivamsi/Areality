package com.passionateburger.areality;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adari on 3/11/2018.
 */

public class HomeFragment extends Fragment {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ProgressBar mProgressView;
    private ViewGroup mContainer;
    private List<FireBaseHelper.Categories> Categories = new ArrayList<>();
    private BaseActivity activity;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public void showProgress(final boolean show) {
        mContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            FragmentTransaction ft = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            ft.remove(this);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        activity = (BaseActivity) getActivity();
        activity.findViewById(R.id.tabs).setVisibility(View.VISIBLE);
        activity.getSupportActionBar().show();
        mProgressView = (ProgressBar) activity.findViewById(R.id.progress);
        mContainer = container;
        showProgress(true);
        new FireBaseHelper.Categories().Tolist(Data -> {
            Categories = Data;
            ViewPager mViewPager = (ViewPager) view.findViewById(R.id.container);
            mSectionsPagerAdapter = new SectionsPagerAdapter(activity.getSupportFragmentManager());
            // Set up the ViewPager with the sections adapter.

            mViewPager.setAdapter(mSectionsPagerAdapter);

            TabLayout tabLayout = (TabLayout) activity.findViewById(R.id.tabs);
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setupWithViewPager(mViewPager);
            showProgress(false);
        });
        return view;
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return ItemFragment.newInstance(Categories.get(position).Key, Categories.get(position).name);
        }

        @Override
        public int getCount() {
            return Categories.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            for (int i = 0; i < getCount(); i++) {
                if (position == i) {
                    return Categories.get(i).name;
                }
            }
            return null;
        }
    }
}