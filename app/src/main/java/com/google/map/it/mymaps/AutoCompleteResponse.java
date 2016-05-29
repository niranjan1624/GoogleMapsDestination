package com.google.map.it.mymaps;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Niranjan on 5/29/2016.
 */
public class AutoCompleteResponse {
    public String status;

    @SerializedName("predictions")
    public List<Suggestion> suggestions;
}
