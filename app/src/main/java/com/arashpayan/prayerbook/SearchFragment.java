package com.arashpayan.prayerbook;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.arashpayan.util.DividerItemDecoration;

public class SearchFragment extends Fragment implements OnPrayerSelectedListener {

    public static String SEARCHPRAYERS_TAG = "SearchPrayers";

    private SearchAdapter mSearchAdapter;
    private CharSequence mQuery = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchAdapter = new SearchAdapter();
        mSearchAdapter.setListener(this);
    }

    public void onStart() {
        super.onStart();

        setHasOptionsMenu(true);
    }

    private void installSearchView() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar ab = activity.getSupportActionBar();
        if (ab == null) {
            throw new RuntimeException("Where's the action bar?");
        }
        ab.setDisplayShowCustomEnabled(true);

        // let the actionbar inflate the search view first so it can style it appropriately.
        // Then we can retrieve it to add our listeners.
        ab.setCustomView(R.layout.search_view);
        SearchView sv = (SearchView) ab.getCustomView();
        sv.setSaveEnabled(true);
        ab.setCustomView(sv);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mQuery = newText;
                final String trimmed = newText.trim();
                if (trimmed.length() < 3) {
                    mSearchAdapter.setCursor(null);
                    return true;
                }

                App.postOnBackgroundThread(new Runnable() {
                    @Override
                    public void run() {
                        String[] keywords = trimmed.split(" ");
                        final Cursor c = Database.getInstance().getPrayersWithKeywords(keywords,
                                Preferences.getInstance(App.getApp()).getEnabledLanguages());
                        App.postOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                mSearchAdapter.setCursor(c);
                            }
                        });
                    }
                });

                return true;
            }
        });
        sv.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });

        if (mQuery != null) {
            sv.setQuery(mQuery, true);
        } else {
            sv.requestFocus();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        installSearchView();

        RecyclerView recyclerView = new RecyclerView(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(mSearchAdapter);

        return recyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
            ab.setDisplayShowCustomEnabled(true);

            // if there's no query saved, then show the keyboard
            if (mQuery == null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowCustomEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }

        return false;
    }

    @Override
    public void onPrayerSelected(long prayerId) {
        // the keyboard might still be present, so dismiss it
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar ab = activity.getSupportActionBar();
        SearchView sv = (SearchView) ab.getCustomView();
        imm.hideSoftInputFromWindow(sv.getWindowToken(), 0);

        Intent intent =  PrayerActivity.newIntent(getContext(), prayerId);
        startActivity(intent);

        getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
    }
}
