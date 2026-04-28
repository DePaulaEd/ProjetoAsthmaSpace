package br.fmu.projetoasthmaspace.Data.Service.Client;

import java.util.List;

import br.fmu.projetoasthmaspace.Core.Domain.Log.ConsultaInfoResponse;
import br.fmu.projetoasthmaspace.Core.Domain.Log.RedefinirSenhaRequest;
import br.fmu.projetoasthmaspace.Core.Domain.Log.VerificarIdentidadeRequest;
import br.fmu.projetoasthmaspace.Core.Domain.Log.VerificarIdentidadeResponse;
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
import br.fmu.projetoasthmaspace.Core.Util.PaginaResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

//    @GET("clientes/me")
//    Call<DadosDetalhamentoCliente> getMeuPerfil(
//            @Header("Authorization") String token
//    );

    @GET("/usuarios/me")
    Call<UsuarioResponse> getUsuarioLogado();


    // -------- DIÁRIO DE SINTOMAS --------
    @POST("diario/cadastro")
    Call<Void> registrarSintoma(@Body DiarioRequest request);

    @GET("diario/listar")
    Call<PaginaResponse<DiarioResponse>> listarDiario(
            @Query("page") int page,
            @Query("size") int size
    );

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
    Call<PaginaResponse<LembreteResponse>> listarLembretes(
            @Query("page") int page,
            @Query("size") int size
    );


    @PUT("lembretes/atualizar/{id}")
    Call<Void> atualizarDados(
            @Path("id") Long id,
            @Body LembreteUpdateRequest request
    );

    @DELETE("/lembretes/deletar/{id}") // Verifique o endpoint correto
    Call<Void> deletarLembrete(@Path("id") Long id);

    // -------- Redefinir Senha --------

    @GET("auth/recuperar-senha/info")
    Call<ConsultaInfoResponse> consultarInfoRecuperacao(@Query("email") String email);

    @POST("auth/recuperar-senha/verificar")
    Call<VerificarIdentidadeResponse> verificarIdentidade(@Body VerificarIdentidadeRequest req);

    @POST("auth/recuperar-senha/redefinir")
    Call<Void> redefinirSenha(@Body RedefinirSenhaRequest req);




}

