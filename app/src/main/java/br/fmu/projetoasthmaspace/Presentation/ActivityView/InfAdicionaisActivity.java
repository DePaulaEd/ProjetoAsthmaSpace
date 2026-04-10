package br.fmu.projetoasthmaspace.Presentation.ActivityView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import br.fmu.projetoasthmaspace.Data.Service.ViaCep.ApiViaCep;
import br.fmu.projetoasthmaspace.Core.Domain.Cliente.DadosCadastroCliente;
import br.fmu.projetoasthmaspace.Core.Domain.Cliente.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.Core.Domain.Endereco.DadosEndereco;
import br.fmu.projetoasthmaspace.Core.Domain.Log.LoginRequest;
import br.fmu.projetoasthmaspace.Core.Domain.Log.TokenResponse;
import br.fmu.projetoasthmaspace.Core.Session.UserSessionManager;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiClient;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiService;
import br.fmu.projetoasthmaspace.Data.Service.ViaCep.ViaCepResponse;
import br.fmu.projetoasthmaspace.databinding.ActivityInfAdicionaisBinding;
import br.fmu.projetoasthmaspace.exception.ErroPadrao;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfAdicionaisActivity extends AppCompatActivity {

    private ActivityInfAdicionaisBinding binding;

    private String nomeUsuario;
    private String email;
    private String cpf;
    private String telefone;
    private String idade;
    private String sexo;
    private String senha;

    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityInfAdicionaisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnVoltar.setOnClickListener(v -> finish());

        nomeUsuario = getIntent().getStringExtra("USER_NAME");
        email       = getIntent().getStringExtra("email");
        cpf         = getIntent().getStringExtra("cpf");
        telefone    = getIntent().getStringExtra("telefone");
        idade       = getIntent().getStringExtra("idade");
        sexo        = getIntent().getStringExtra("sexo");
        senha       = getIntent().getStringExtra("senha");

        binding.btnConcluir.setOnClickListener(v -> enviarCadastro());

        //  Mostra/esconde campo "Outros" conforme checkbox
        binding.checkOutros.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.editTextOutrosRespiratorios.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) binding.editTextOutrosRespiratorios.setText("");
        });

        // Busca CEP automaticamente ao sair do campo
        binding.editTextCep.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String cep = binding.editTextCep.getText().toString().trim();
                if (cep.matches("\\d{8}")) {
                    buscarCep(cep);
                }
            }
        });
    }

    //  Coleta checkboxes e concatena numa string para o backend
    private String coletarProblemasRespiratorios() {
        List<String> selecionados = new ArrayList<>();

        if (binding.checkAsma.isChecked())      selecionados.add("Asma");
        if (binding.checkRinite.isChecked())    selecionados.add("Rinite");
        if (binding.checkBronquite.isChecked()) selecionados.add("Bronquite");
        if (binding.checkDpoc.isChecked())      selecionados.add("DPOC");

        if (binding.checkOutros.isChecked()) {
            String descricao = binding.editTextOutrosRespiratorios.getText().toString().trim();
            if (!descricao.isEmpty()) {
                selecionados.add("Outros: " + descricao);
            }
        }

        return String.join(", ", selecionados); // ex: "Asma, Rinite, Outros: sinusite"
    }

    private void buscarCep(String cep) {
        ApiViaCep.getService().buscarCep(cep).enqueue(new Callback<ViaCepResponse>() {
            @Override
            public void onResponse(Call<ViaCepResponse> call, Response<ViaCepResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ViaCepResponse dados = response.body();

                    if ("true".equals(dados.erro)) {
                        Toast.makeText(InfAdicionaisActivity.this,
                                "CEP não encontrado", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    binding.editTextLogradouro.setText(dados.logradouro);
                    binding.editTextBairro.setText(dados.bairro);
                    binding.editTextCidade.setText(dados.localidade);
                    binding.editTextEstado.setText(dados.uf);

                    if (dados.complemento != null && !dados.complemento.isEmpty()) {
                        binding.editTextComplemento.setText(dados.complemento);
                    }

                    binding.editTextNumero.requestFocus();
                }
            }

            @Override
            public void onFailure(Call<ViaCepResponse> call, Throwable t) {
                Toast.makeText(InfAdicionaisActivity.this,
                        "Erro ao buscar CEP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarCadastro() {
        DadosEndereco endereco = new DadosEndereco(
                binding.editTextLogradouro.getText().toString(),
                binding.editTextBairro.getText().toString(),
                binding.editTextCep.getText().toString(),
                binding.editTextComplemento.getText().toString(),
                binding.editTextNumero.getText().toString(),
                binding.editTextEstado.getText().toString(),
                binding.editTextCidade.getText().toString()
        );

        DadosCadastroCliente dados = new DadosCadastroCliente(
                nomeUsuario,
                email,
                telefone,
                cpf,
                senha,
                Integer.parseInt(idade),
                sexo,
                endereco,
                coletarProblemasRespiratorios(),
                binding.editTextMedicamentos.getText().toString(),
                binding.editTextContatoEmerg.getText().toString()
        );

        api = ApiClient.getApiService(getApplicationContext());

        api.cadastrarCliente(dados).enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call,
                                   Response<DadosDetalhamentoCliente> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DadosDetalhamentoCliente usuarioCriado = response.body();
                    UserSessionManager session = new UserSessionManager(InfAdicionaisActivity.this);
                    session.saveNome(usuarioCriado.getNome());
                    fazerLoginAutomatico(session, email, senha);
                } else {
                    try {
                        Gson gson = new Gson();
                        String erroJson = response.errorBody().string();
                        ErroPadrao erro = gson.fromJson(erroJson, ErroPadrao.class);
                        Toast.makeText(InfAdicionaisActivity.this, erro.mensagem, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(InfAdicionaisActivity.this,
                                "Erro ao processar resposta do servidor",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Toast.makeText(InfAdicionaisActivity.this,
                        "Falha de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fazerLoginAutomatico(UserSessionManager session, String email, String senha) {
        LoginRequest loginRequest = new LoginRequest(email, senha);

        api.login(loginRequest).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    session.saveToken(response.body().token);
                    Toast.makeText(InfAdicionaisActivity.this,
                            "Cadastro realizado!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(InfAdicionaisActivity.this,
                            "Cadastro realizado! Faça login para continuar.",
                            Toast.LENGTH_LONG).show();
                }

                Intent intent = new Intent(InfAdicionaisActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Toast.makeText(InfAdicionaisActivity.this,
                        "Cadastro realizado! Faça login para continuar.",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(InfAdicionaisActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}