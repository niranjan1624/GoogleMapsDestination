package com.google.map.it.mymaps;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by Niranjan on 5/29/2016.
 */
public class MapitApi {
    private static final String PLACES_URL = "https://maps.googleapis.com/maps/api";

    public static MapItService getGoogleApiService() {
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(PLACES_URL);

        return builder.build().create(MapItService.class);
    }

    /**
     * A request interceptor used to modify all requests sent through this   service. Currently,
     * this is responsible for adding a User-Agent and X-Authorization header to  the request.
     */
    private RequestInterceptor requestInterceptor = new RequestInterceptor() {
       /* String credentials = "username:password";

        private final String auth =
                "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);*/

        @Override
        public void intercept(RequestInterceptor.RequestFacade request) {
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");
        }
    };
}