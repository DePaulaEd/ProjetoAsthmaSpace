package br.fmu.projetoasthmaspace.Data.Service.ViaCep;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

// Novo: ApiServiceViaCep.java
public interface ApiServiceViaCep {
    @GET("{cep}/json/")
    Call<ViaCepResponse> buscarCep(@Path("cep") String cep);
}
