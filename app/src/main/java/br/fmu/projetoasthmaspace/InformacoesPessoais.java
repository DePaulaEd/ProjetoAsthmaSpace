package br.fmu.projetoasthmaspace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import br.fmu.projetoasthmaspace.Domain.EditarInformacoes;
import br.fmu.projetoasthmaspace.Service.ApiClient;
import br.fmu.projetoasthmaspace.Service.ApiService;
import br.fmu.projetoasthmaspace.Domain.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.databinding.ActivityInformacoesPessoaisBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InformacoesPessoais extends AppCompatActivity {

    private ActivityInformacoesPessoaisBinding binding;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInformacoesPessoaisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences("APP", MODE_PRIVATE);
        String token = prefs.getString("TOKEN", null);

        api = ApiClient.getApiService(token);


        Log.d("INFO_PESSOAIS", "Tela aberta — sincronizando com backend");
        sincronizarBackend();
        configurarBotoes();
    }

    private void configurarBotoes() {
        binding.btnEditar.setOnClickListener(v -> {
            Intent i = new Intent(this, EditarInformacoes.class);
            startActivity(i);
        });
    }

    private void sincronizarBackend() {
        api.getMeuPerfil().enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call, Response<DadosDetalhamentoCliente> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("INFO_PESSOAIS", "Erro backend: " + response.code());
                    carregarLocal();
                    return;
                }

                DadosDetalhamentoCliente dados = response.body();
                salvarLocal(dados);
                atualizarUI(dados);
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Log.e("INFO_PESSOAIS", "Falha: " + t.getMessage());
                carregarLocal();
            }
        });
    }


    private void salvarLocal(DadosDetalhamentoCliente d) {
        SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();

        editor.putString("user_name", d.getNome());
        editor.putString("user_email", d.getEmail());
        editor.putString("user_phone", d.getTelefone());
        editor.putString("user_cpf", d.getCpf());
        editor.putString("user_sexo", d.getSexo());
        editor.putString("user_medicamentos", d.getMedicamentos());
        editor.putString("user_problemas_respiratorios", d.getProblema_respiratorio());
        editor.putString("user_contato_emergencia", d.getContatoEmergencia());
        editor.putInt("user_idade", d.getIdade());

        editor.apply();

        Log.d("INFO_PESSOAIS", "Dados salvos no SharedPreferences.");
    }

    private void carregarLocal() {
        Log.d("INFO_PESSOAIS", "Carregando dados do SharedPreferences…");

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        DadosDetalhamentoCliente dados = new DadosDetalhamentoCliente();
        dados.setNome(prefs.getString("user_name", "---"));
        dados.setEmail(prefs.getString("user_email", "---"));
        dados.setTelefone(prefs.getString("user_phone", "---"));
        dados.setCpf(prefs.getString("user_cpf", "---"));
        dados.setSexo(prefs.getString("user_sexo", "---"));
        dados.setMedicamentos(prefs.getString("user_medicamentos", "---"));
        dados.setProblema_respiratorio(prefs.getString("user_problemas_respiratorios", "---"));
        dados.setContatoEmergencia(prefs.getString("user_contato_emergencia", "---"));
        dados.setIdade(prefs.getInt("user_idade", 0));

        atualizarUI(dados);
    }

    private void atualizarUI(DadosDetalhamentoCliente d) {

        binding.txtNome.setText(safe(d.getNome()));
        binding.txtEmail.setText(safe(d.getEmail()));
        binding.txtTelefone.setText(safe(d.getTelefone()));
        binding.txtCPF.setText(safe(d.getCpf()));
        binding.txtSexo.setText(safe(d.getSexo()));

        binding.txtDataNascimento.setText(
                safeInt(d.getIdade())
        );

        binding.txtMedicamentos.setText(safe(d.getMedicamentos()));
        binding.txtRespiratorios.setText(safe(d.getProblema_respiratorio()));
        binding.txtEmergencia.setText(safe(d.getContatoEmergencia()));

        Log.d("INFO_PESSOAIS", "UI atualizada!");
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "---" : s;
    }

    private String safeInt(int v) {
        return (v == 0) ? "---" : String.valueOf(v);
    }
}
