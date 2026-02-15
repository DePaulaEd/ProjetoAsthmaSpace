package br.fmu.projetoasthmaspace.Service;

import android.content.Context;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiOpenWeather {

    private static final String BASE_URL =
            "https://api.openweathermap.org/data/2.5/";

    private static final String API_KEY = "756f445ac50f45bd64b41eb0bbe0a3ac";

    private static Retrofit retrofit;

    public static ApiServiceOpenWeather getApiService() {

        if (retrofit == null) {

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        HttpUrl url = original.url().newBuilder()
                                .addQueryParameter("appid", API_KEY)
                                .build();

                        Request request = original.newBuilder()
                                .url(url)
                                .build();

                        return chain.proceed(request);
                    })
                    .addInterceptor(
                            new HttpLoggingInterceptor()
                                    .setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }

        return retrofit.create(ApiServiceOpenWeather.class);
    }

}

