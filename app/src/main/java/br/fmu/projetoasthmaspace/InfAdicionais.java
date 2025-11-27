package br.fmu.projetoasthmaspace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import br.fmu.projetoasthmaspace.Domain.DadosCadastroCliente;
import br.fmu.projetoasthmaspace.Domain.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.Domain.DadosEndereco;
import br.fmu.projetoasthmaspace.Service.ApiClient;
import br.fmu.projetoasthmaspace.Service.ApiService;
import br.fmu.projetoasthmaspace.databinding.ActivityInfAdicionaisBinding;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityInfAdicionaisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Recebendo dados da tela anterior
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

        // Criar objeto Endereço
        DadosEndereco endereco = new DadosEndereco(
                binding.editTextLogradouro.getText().toString(),
                binding.editTextBairro.getText().toString(),
                binding.editTextCep.getText().toString(),
                binding.editTextComplemento.getText().toString(),
                binding.editTextNumero.getText().toString(),
                binding.editTextEstado.getText().toString(),
                binding.editTextCidade.getText().toString()
        );

        // Criar o corpo completo do JSON
        DadosCadastroCliente dados = new DadosCadastroCliente(
                nomeUsuario,                // nome
                email,                      // email
                telefone,                   // telefone
                cpf,                        // cpf
                senha,       // senha (ou receba da tela)
                Integer.parseInt(idade),    // idade
                sexo,                       // sexo
                endereco,                   // objeto endereço
                binding.editTextProblemRespiratorios.getText().toString(),
                binding.editTextMedicamentos.getText().toString(),
                binding.editTextContatoEmerg.getText().toString()
        );

        ApiService api = ApiClient.getApiService();


        api.cadastrarCliente(dados).enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call, Response<DadosDetalhamentoCliente> response) {
                if (response.isSuccessful()) {

                    DadosDetalhamentoCliente usuarioCriado = response.body();

                    Toast.makeText(InfAdicionais.this, "Cadastro realizado!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(InfAdicionais.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(InfAdicionais.this, "Erro ao cadastrar: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Toast.makeText(InfAdicionais.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

