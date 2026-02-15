package br.fmu.projetoasthmaspace.ActivityView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.fmu.projetoasthmaspace.Domain.AtualizarRequest;
import br.fmu.projetoasthmaspace.Domain.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.Domain.Endereco;
import br.fmu.projetoasthmaspace.Service.ApiClient;
import br.fmu.projetoasthmaspace.Service.ApiService;
import br.fmu.projetoasthmaspace.databinding.ActivityEnderecoBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnderecoActivity extends AppCompatActivity {

    private ActivityEnderecoBinding binding;
    private ApiService api;
    private Endereco enderecoAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnderecoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String token = getSharedPreferences("APP", MODE_PRIVATE).getString("TOKEN", null);
        api = ApiClient.getApiService(getApplicationContext());


        carregarEnderecoBackend();

        binding.btnVoltar.setOnClickListener(v -> finish());
//        binding.btnSalvar.setOnClickListener(v -> salvarEndereco());
    }

    private void carregarEnderecoBackend() {
        api.getMeuPerfil().enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call, Response<DadosDetalhamentoCliente> response) {
                if (response.isSuccessful() && response.body() != null) {
                    enderecoAtual = response.body().getEndereco();
                    atualizarUI(enderecoAtual);
                } else {
                    Toast.makeText(EnderecoActivity.this, "Erro ao buscar endereço", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Toast.makeText(EnderecoActivity.this, "Falha na comunicação", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarUI(Endereco e) {
        if (e == null) return;
        binding.edtLogradouro.setText(safe(e.getLogradouro()));
        binding.edtNumero.setText(safe(e.getNumero()));
        binding.edtComplemento.setText(safe(e.getComplemento()));
        binding.edtBairro.setText(safe(e.getBairro()));
        binding.edtCidade.setText(safe(e.getCidade()));
        binding.edtUF.setText(safe(e.getUf()));
        binding.edtCEP.setText(safe(e.getCep()));
    }

    private void salvarEndereco() {
        if (enderecoAtual == null) enderecoAtual = new Endereco();

        enderecoAtual.setLogradouro(binding.edtLogradouro.getText().toString());
        enderecoAtual.setNumero(binding.edtNumero.getText().toString());
        enderecoAtual.setComplemento(binding.edtComplemento.getText().toString());
        enderecoAtual.setBairro(binding.edtBairro.getText().toString());
        enderecoAtual.setCidade(binding.edtCidade.getText().toString());
        enderecoAtual.setUf(binding.edtUF.getText().toString());
        enderecoAtual.setCep(binding.edtCEP.getText().toString());

        AtualizarRequest request = new AtualizarRequest();
        request.setEndereco(enderecoAtual);

        api.atualizarPerfil(request).enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call, Response<DadosDetalhamentoCliente> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EnderecoActivity.this, "Endereço atualizado!", Toast.LENGTH_SHORT).show();
                    enderecoAtual = response.body().getEndereco(); // atualiza em memória
                } else {
                    Toast.makeText(EnderecoActivity.this, "Erro ao salvar endereço", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Toast.makeText(EnderecoActivity.this, "Falha na comunicação", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        enderecoAtual = null;
        binding = null;
        api = null;
        Log.d("ENDERECO", "Dados destruídos ao sair da tela");
    }
}
