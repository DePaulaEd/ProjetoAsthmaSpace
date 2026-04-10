package br.fmu.projetoasthmaspace.Core.Session;

import android.content.Context;
import android.util.Log;

import br.fmu.projetoasthmaspace.Core.Domain.Cliente.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiClient;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserServiceHelper {

    public interface NomeCallback {
        void onSuccess(String nomeCompleto);
        void onError(String erro);
    }

    public static void buscarNomeUsuario(
            Context context,
            String token,
            NomeCallback callback) {

        ApiService api = ApiClient.getApiService(context);

        api.getMeuPerfil("Bearer " + token)
                .enqueue(new Callback<DadosDetalhamentoCliente>() {

                    @Override
                    public void onResponse(Call<DadosDetalhamentoCliente> call,
                                           Response<DadosDetalhamentoCliente> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            String nome = response.body().getNome();

                            // Salva na sessão
                            UserSessionManager session =
                                    new UserSessionManager(context);

                            session.saveNome(nome);

                            callback.onSuccess(nome);

                        } else {
                            callback.onError("Erro ao buscar perfil.");
                        }
                    }

                    @Override
                    public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                        Log.e("USER_HELPER", t.getMessage());
                        callback.onError(t.getMessage());
                    }
                });
    }
}