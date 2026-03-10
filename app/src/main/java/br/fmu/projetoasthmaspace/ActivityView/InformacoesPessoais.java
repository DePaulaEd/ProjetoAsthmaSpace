package br.fmu.projetoasthmaspace.ActivityView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.fmu.projetoasthmaspace.Domain.AtualizarRequest;
import br.fmu.projetoasthmaspace.Domain.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.Domain.Endereco;
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

        api = ApiClient.getApiService(getApplicationContext());

        Log.d("INFO_PESSOAIS", "Tela aberta — sincronizando com backend");

        carregarDadosBackend();

        // Voltar fecha a tela
        binding.btnVoltar.setOnClickListener(v -> finish());

        // Salvar chama o backend
        binding.btnSalvar.setOnClickListener(v -> salvarAlteracoes());
    }

    private void carregarDadosBackend() {
        api.getMeuPerfil().enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call,
                                   Response<DadosDetalhamentoCliente> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dadosAtuais = response.body();
                    atualizarUI(dadosAtuais);
                } else {
                    Toast.makeText(InformacoesPessoais.this,
                            "Erro ao buscar dados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Log.e("INFO_PESSOAIS", "Falha: " + t.getMessage());
                Toast.makeText(InformacoesPessoais.this,
                        "Falha na comunicação", Toast.LENGTH_SHORT).show();
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

        // Endereço
        if (d.getEndereco() != null) {
            binding.edtCEP.setText(safe(d.getEndereco().getCep()));
            binding.edtLogradouro.setText(safe(d.getEndereco().getLogradouro()));
            binding.edtNumero.setText(safe(d.getEndereco().getNumero()));
            binding.edtComplemento.setText(safe(d.getEndereco().getComplemento()));
            binding.edtBairro.setText(safe(d.getEndereco().getBairro()));
            binding.edtCidade.setText(safe(d.getEndereco().getCidade()));
            binding.edtUF.setText(safe(d.getEndereco().getUf()));
        }
    }

    private void salvarAlteracoes() {
        if (dadosAtuais == null) return;

        binding.btnSalvar.setEnabled(false);
        binding.btnSalvar.setText("Salvando...");

        AtualizarRequest request = new AtualizarRequest();
        request.nome              = binding.edtNome.getText().toString().trim();
        request.telefone          = binding.edtTelefone.getText().toString().trim();
        request.sexo              = binding.edtSexo.getText().toString().trim();
        request.idade             = parseInt(binding.edtIdade.getText().toString().trim());
        request.medicamentos      = binding.edtMedicamentos.getText().toString().trim();
        request.contatoEmergencia = binding.edtEmergencia.getText().toString().trim();

        Endereco end = new Endereco();
        end.setCep(binding.edtCEP.getText().toString().trim());
        end.setLogradouro(binding.edtLogradouro.getText().toString().trim());
        end.setNumero(binding.edtNumero.getText().toString().trim());
        end.setComplemento(binding.edtComplemento.getText().toString().trim());
        end.setBairro(binding.edtBairro.getText().toString().trim());
        end.setCidade(binding.edtCidade.getText().toString().trim());
        end.setUf(binding.edtUF.getText().toString().trim());
        request.endereco = end;

        api.atualizarPerfil(request).enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call,
                                   Response<DadosDetalhamentoCliente> response) {
                binding.btnSalvar.setEnabled(true);
                binding.btnSalvar.setText("Salvar Alterações");

                if (response.isSuccessful() && response.body() != null) {
                    dadosAtuais = response.body();
                    atualizarUI(dadosAtuais);
                    Toast.makeText(InformacoesPessoais.this,
                            "✓ Alterações salvas!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(InformacoesPessoais.this,
                            "Erro ao salvar alterações", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                binding.btnSalvar.setEnabled(true);
                binding.btnSalvar.setText("Salvar Alterações");
                Log.e("INFO_PESSOAIS", "Falha: " + t.getMessage()); // ← adiciona isso
                Log.e("INFO_PESSOAIS", "Causa: " + t.getCause());   // ← e isso
                Toast.makeText(InformacoesPessoais.this,
                        "Falha na comunicação", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s;
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