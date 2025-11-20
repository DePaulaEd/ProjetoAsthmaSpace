package br.fmu.projetoasthmaspace.Service;

import br.fmu.projetoasthmaspace.Domain.AtualizarRequest;
import br.fmu.projetoasthmaspace.Domain.ClienteResponse;
import br.fmu.projetoasthmaspace.Domain.DadosCadastroCliente;
import br.fmu.projetoasthmaspace.Domain.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.Domain.LoginRequest;
import br.fmu.projetoasthmaspace.Domain.TokenResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {

    @POST("login")
    Call<TokenResponse> login(@Body LoginRequest request);

    @POST("clientes/cadastro")
    Call<DadosDetalhamentoCliente> cadastrarCliente(@Body DadosCadastroCliente request);

    @PUT("clientes/atualizar")
    Call<ClienteResponse> atualizarCliente(@Body AtualizarRequest request);

}

