package br.fmu.projetoasthmaspace.Data.Service.ViaCep;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Novo: ApiViaCep.java
public class ApiViaCep {
    private static final String BASE_URL = "https://viacep.com.br/ws/";
    private static Retrofit retrofit;

    public static ApiServiceViaCep getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiServiceViaCep.class);
    }
}
