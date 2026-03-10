package br.fmu.projetoasthmaspace.ActivityView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import br.fmu.projetoasthmaspace.Domain.DadosCadastroCliente;
import br.fmu.projetoasthmaspace.Domain.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.Domain.DadosEndereco;
import br.fmu.projetoasthmaspace.Domain.LoginRequest;
import br.fmu.projetoasthmaspace.Domain.TokenResponse;
import br.fmu.projetoasthmaspace.Domain.UserSessionManager;
import br.fmu.projetoasthmaspace.Service.ApiClient;
import br.fmu.projetoasthmaspace.Service.ApiService;
import br.fmu.projetoasthmaspace.databinding.ActivityInfAdicionaisBinding;
import br.fmu.projetoasthmaspace.exception.ErroPadrao;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfAdicionais extends AppCompatActivity {

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
        email = getIntent().getStringExtra("email");
        cpf = getIntent().getStringExtra("cpf");
        telefone = getIntent().getStringExtra("telefone");
        idade = getIntent().getStringExtra("idade");
        sexo = getIntent().getStringExtra("sexo");
        senha = getIntent().getStringExtra("senha");

        binding.btnConcluir.setOnClickListener(v -> enviarCadastro());
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
                binding.editTextProblemRespiratorios.getText().toString(),
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

                    UserSessionManager session = new UserSessionManager(InfAdicionais.this);
                    session.saveNome(usuarioCriado.getNome());

                    // 🔥 Login automático para obter o token
                    fazerLoginAutomatico(session, email, senha);

                } else {
                    try {
                        Gson gson = new Gson();
                        String erroJson = response.errorBody().string();
                        ErroPadrao erro = gson.fromJson(erroJson, ErroPadrao.class);
                        Toast.makeText(InfAdicionais.this, erro.mensagem, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(InfAdicionais.this,
                                "Erro ao processar resposta do servidor",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Toast.makeText(InfAdicionais.this,
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
                    Toast.makeText(InfAdicionais.this, "Cadastro realizado!", Toast.LENGTH_SHORT).show();
                } else {
                    // Cadastro foi ok, mas login automático falhou
                    // O usuário terá que fazer login manualmente
                    Toast.makeText(InfAdicionais.this,
                            "Cadastro realizado! Faça login para continuar.",
                            Toast.LENGTH_LONG).show();
                }

                // Navega para MainActivity em ambos os casos
                Intent intent = new Intent(InfAdicionais.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Toast.makeText(InfAdicionais.this,
                        "Cadastro realizado! Faça login para continuar.",
                        Toast.LENGTH_LONG).show();

                // Navega mesmo assim
                Intent intent = new Intent(InfAdicionais.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}