package br.fmu.projetoasthmaspace.Service;

import java.util.List;

import br.fmu.projetoasthmaspace.Domain.AtualizarRequest;
import br.fmu.projetoasthmaspace.Domain.ClienteResponse;
import br.fmu.projetoasthmaspace.Domain.DadosCadastroCliente;
import br.fmu.projetoasthmaspace.Domain.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.Domain.DiarioRequest;
import br.fmu.projetoasthmaspace.Domain.DiarioResponse;
import br.fmu.projetoasthmaspace.Domain.Lembrete;
import br.fmu.projetoasthmaspace.Domain.LembreteReceiver;
import br.fmu.projetoasthmaspace.Domain.LembreteRequest;
import br.fmu.projetoasthmaspace.Domain.LembreteResponse;
import br.fmu.projetoasthmaspace.Domain.LembreteUpdateRequest;
import br.fmu.projetoasthmaspace.Domain.LoginRequest;
import br.fmu.projetoasthmaspace.Domain.TokenResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // -------- LOGIN -------- //
    @POST("login")
    Call<TokenResponse> login(@Body LoginRequest request);

    // -------- CLIENTES  --------
    @POST("clientes/cadastro")
    Call<DadosDetalhamentoCliente> cadastrarCliente(@Body DadosCadastroCliente request);

    @PUT("clientes/atualizar")
    Call<ClienteResponse> atualizarCliente(@Body AtualizarRequest request);

    @PUT("clientes/atualizar")
    Call<DadosDetalhamentoCliente> atualizarPerfil(@Body AtualizarRequest request);

    @GET("clientes/me")
    Call<DadosDetalhamentoCliente> getMeuPerfil();


    // -------- DIÁRIO DE SINTOMAS --------
    @POST("diario/cadastro")
    Call<Void> registrarSintoma(@Body DiarioRequest request);

    @GET("diario/listar")
    Call<List<DiarioResponse>> listarDiario();

    @PUT("diario/atualizar")
    Call<DiarioResponse> atualizarDiario(@Path("id") Long id, @Body DiarioRequest request);

    @DELETE("diario/delete/{id}")
    Call<Void> deletarDiario(@Path("id") Long id);



    // -------- LEMBRETES --------


    @POST("lembretes/cadastro")
    Call<Void> registrarLembrete (@Body LembreteRequest request);
    @GET("lembretes/listar")
    Call<List<LembreteResponse>> listarLembretes();


    @PUT("/lembretes/atualizar") // Verifique o endpoint correto
    Call<Void> atualizarConclusao(@Body LembreteUpdateRequest request);

    @PUT("lembretes/atualizar")
    Call<Void> atualizarDados(@Body LembreteUpdateRequest request);


    @DELETE("/lembretes/deletar/{id}") // Verifique o endpoint correto
    Call<Void> deletarLembrete(@Path("id") Long id);









}

