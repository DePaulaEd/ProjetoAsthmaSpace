package br.fmu.projetoasthmaspace.ActivityView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.fmu.projetoasthmaspace.Domain.AtualizarRequest;
import br.fmu.projetoasthmaspace.Domain.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.Service.ApiClient;
import br.fmu.projetoasthmaspace.Service.ApiService;
import br.fmu.projetoasthmaspace.databinding.ActivityInformacoesPessoaisBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InformacoesPessoais extends AppCompatActivity {

    private ActivityInformacoesPessoaisBinding binding;
    private ApiService api;
    private DadosDetalhamentoCliente dadosAtuais;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInformacoesPessoaisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String token = getSharedPreferences("APP", MODE_PRIVATE).getString("TOKEN", null);
        api = ApiClient.getApiService(token);

        Log.d("INFO_PESSOAIS", "Tela aberta — sincronizando com backend");

        carregarDadosBackend();
        binding.btnProximo.setOnClickListener(v -> {
            Intent intent = new Intent(InformacoesPessoais.this, MainActivity.class);
            startActivity(intent);
        });

//        binding.btnSalvar.setOnClickListener(v -> salvarAlteracoes());

    }

    private void carregarDadosBackend() {
        api.getMeuPerfil().enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call, Response<DadosDetalhamentoCliente> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dadosAtuais = response.body();
                    atualizarUI(dadosAtuais);
                } else {
                    Toast.makeText(InformacoesPessoais.this, "Erro ao buscar dados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Log.e("INFO_PESSOAIS", "Falha: " + t.getMessage());
                Toast.makeText(InformacoesPessoais.this, "Falha na comunicação", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarUI(DadosDetalhamentoCliente d) {
        binding.edtNome.setText(safe(d.getNome()));
        binding.txtEmail.setText(safe(d.getEmail()));
        binding.edtTelefone.setText(safe(d.getTelefone()));
        binding.txtCPF.setText(safe(d.getCpf()));
        binding.edtSexo.setText(safe(d.getSexo()));
        binding.edtIdade.setText(safeInt(d.getIdade()));
        binding.edtMedicamentos.setText(safe(d.getMedicamentos()));
        binding.edtEmergencia.setText(safe(d.getContatoEmergencia()));
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s;
    }

    private void salvarAlteracoes() {
        if (dadosAtuais == null) return;

        // Monta o request de atualização
        AtualizarRequest request = new AtualizarRequest();
        request.nome = binding.edtNome.getText().toString();
        request.telefone = binding.edtTelefone.getText().toString();
        request.sexo = binding.edtSexo.getText().toString();
        request.idade = parseInt(binding.edtIdade.getText().toString());
        request.medicamentos = binding.edtMedicamentos.getText().toString();
        request.contatoEmergencia = binding.edtEmergencia.getText().toString();


        api.atualizarPerfil(request).enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call, Response<DadosDetalhamentoCliente> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(InformacoesPessoais.this, "Alterações salvas!", Toast.LENGTH_SHORT).show();
                    // Atualiza dadosAtuais em memória
                    dadosAtuais = response.body();
                } else {
                    Toast.makeText(InformacoesPessoais.this, "Erro ao salvar alterações", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Toast.makeText(InformacoesPessoais.this, "Falha na comunicação", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String safeInt(Integer v) {
        return (v == null || v == 0) ? "" : String.valueOf(v);
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return null; }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dadosAtuais = null;
        binding = null;
        api = null;
        Log.d("INFO_PESSOAIS", "Dados destruídos ao sair da tela");
    }

}
