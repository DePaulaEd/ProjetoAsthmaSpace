package br.fmu.projetoasthmaspace.Data.Service.QualityAir;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiServiceOpenWeather {

    @GET("air_pollution")
    Call<AirResponse> getAirQuality(
            @Query("lat") double lat,
            @Query("lon") double lon
    );
}
