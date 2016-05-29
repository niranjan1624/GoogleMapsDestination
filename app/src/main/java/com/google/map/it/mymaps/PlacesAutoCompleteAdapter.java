package com.google.map.it.mymaps;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Niranjan on 5/29/2016.
 */

public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> {
    private static final String API_KEY = "API_KEY";
    private List<Suggestion> resultList;
    private Context mContext;

    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index).description;
    }

    public void autocomplete(String charseq) {
        if (charseq != null && charseq.length() > 3) {
            fetchSuggestions(charseq);
        }
    }

    private void fetchSuggestions(String charseq) {
        MapitApi.getGoogleApiService().fetchPlaces(charseq, "geocode", "en", true, API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<AutoCompleteResponse>() {
                    @Override
                    public void call(AutoCompleteResponse response) {
                        resultList = response.suggestions;
                        notifyDataSetChanged();
                        Log.d("DEBUG", response.suggestions.size() + " ");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d("TEST", throwable.getLocalizedMessage());
                        throwable.printStackTrace();
                        Toast.makeText(mContext,
                                "No internet try enter Latitude and longitude", Toast.LENGTH_LONG).show();
                    }
                });
    }


}