package com.google.map.it.mymaps;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Niranjan on 5/29/2016.
 */
public interface MapItService {

    @GET("/place/autocomplete/json")
    Observable<AutoCompleteResponse> fetchPlaces(@Query("input") String charseq,
                                                 @Query("types") String type,
                                                 @Query("language") String lang,
                                                 @Query("sensor") boolean sensor,
                                                 @Query("key") String apiKey
    );

}
