package br.fmu.projetoasthmaspace.Data.Service.Client;

import java.util.List;

import br.fmu.projetoasthmaspace.Core.Util.AlterarSenhaRequest;
import br.fmu.projetoasthmaspace.Core.Util.AtualizarRequest;
import br.fmu.projetoasthmaspace.Core.Domain.Cliente.ClienteResponse;
import br.fmu.projetoasthmaspace.Core.Domain.Cliente.DadosCadastroCliente;
import br.fmu.projetoasthmaspace.Core.Domain.Cliente.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.Core.Domain.Diario.DiarioRequest;
import br.fmu.projetoasthmaspace.Core.Domain.Diario.DiarioResponse;
import br.fmu.projetoasthmaspace.Core.Domain.Lembretes.LembreteRequest;
import br.fmu.projetoasthmaspace.Core.Domain.Lembretes.LembreteResponse;
import br.fmu.projetoasthmaspace.Core.Domain.Lembretes.LembreteUpdateRequest;
import br.fmu.projetoasthmaspace.Core.Domain.Log.LoginRequest;
import br.fmu.projetoasthmaspace.Core.Domain.Log.TokenResponse;
import br.fmu.projetoasthmaspace.Core.Domain.Log.UsuarioResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
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

    @PUT("clientes/senha")
    Call<Void> alterarSenha(@Body AlterarSenhaRequest request);

    @DELETE("clientes/inativar/{id}")
    Call<Void> inativarCliente(@Path("id") Long id);

    @GET("clientes/me")
    Call<DadosDetalhamentoCliente> getMeuPerfil();

    @GET("clientes/me/id")
    Call<Long> getMeuId();

    @GET("clientes/me")
    Call<DadosDetalhamentoCliente> getMeuPerfil(
            @Header("Authorization") String token
    );

    @GET("/usuarios/me")
    Call<UsuarioResponse> getUsuarioLogado();


    // -------- DIÁRIO DE SINTOMAS --------
    @POST("diario/cadastro")
    Call<Void> registrarSintoma(@Body DiarioRequest request);

    @GET("diario/listar")
    Call<List<DiarioResponse>> listarDiario();

    @PUT("diario/atualizar/{id}")
    Call<DiarioResponse> atualizarDiario(@Path("id") Long id, @Body DiarioRequest request);

    @DELETE("diario/delete/{id}")
    Call<Void> deletarDiario(@Path("id") Long id);

    // -------- RELATÓRIO DE SINTOMAS  PDF --------
    @GET("relatorios/diario/{meses}")
    Call<ResponseBody> gerarPdfDiario(
            @Path("meses") int meses
    );



    // -------- LEMBRETES --------


    @POST("lembretes/cadastro")
    Call<Void> registrarLembrete (@Body LembreteRequest request);
    @GET("lembretes/listar")
    Call<List<LembreteResponse>> listarLembretes();


    @PUT("lembretes/atualizar/{id}")
    Call<Void> atualizarDados(
            @Path("id") Long id,
            @Body LembreteUpdateRequest request
    );

    @DELETE("/lembretes/deletar/{id}") // Verifique o endpoint correto
    Call<Void> deletarLembrete(@Path("id") Long id);




}

