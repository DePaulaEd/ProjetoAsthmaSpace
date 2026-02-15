package br.fmu.projetoasthmaspace.ActivityView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import br.fmu.projetoasthmaspace.Domain.AtualizarRequest;
import br.fmu.projetoasthmaspace.Domain.Endereco;
import br.fmu.projetoasthmaspace.Domain.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.Service.ApiClient;
import br.fmu.projetoasthmaspace.Service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarInformacoes extends AppCompatActivity {

    // =========================
    // CAMPOS DA TELA
    // =========================
    private EditText edtNome, edtTelefone, edtSexo, edtIdade;
    private EditText edtMedicamentos, edtEmergencia;
    private EditText edtLogradouro, edtNumero, edtComplemento, edtBairro, edtCidade, edtUF, edtCEP;

    private TextView txtEmail, txtCPF;
    private Button btnSalvar;

    private ApiService api;
    private static final String TAG = "EDITAR_INFO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_informacoes);

        Log.d(TAG, "onCreate da EditarInformacoes");

        inicializarViews();
        inicializarApi();

        carregarPerfil();
        btnSalvar.setOnClickListener(v -> salvarDados());
    }


    private void inicializarViews() {
        edtNome = findViewById(R.id.edtNome);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtSexo = findViewById(R.id.edtSexo);
        edtIdade = findViewById(R.id.edtIdade);
        edtMedicamentos = findViewById(R.id.edtMedicamentos);
        edtEmergencia = findViewById(R.id.edtEmergencia);

        edtLogradouro = findViewById(R.id.edtLogradouro);
        edtNumero = findViewById(R.id.edtNumero);
        edtComplemento = findViewById(R.id.edtComplemento);
        edtBairro = findViewById(R.id.edtBairro);
        edtCidade = findViewById(R.id.edtCidade);
        edtUF = findViewById(R.id.edtUF);
        edtCEP = findViewById(R.id.edtCEP);

        txtEmail = findViewById(R.id.txtEmail);
        txtCPF = findViewById(R.id.txtCPF);

//        btnSalvar = findViewById(R.id.btnSalvar);
    }

    private void inicializarApi() {
        String token = getSharedPreferences("APP", MODE_PRIVATE).getString("TOKEN", null);
        api = ApiClient.getApiService(getApplicationContext());

    }


    private void carregarPerfil() {
        api.getMeuPerfil().enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call,
                                   Response<DadosDetalhamentoCliente> response) {

                Log.d(TAG, "Resposta JSON: " + new Gson().toJson(response.body()));

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Erro backend: " + response.code());
                    Toast.makeText(EditarInformacoes.this,
                            "Erro ao carregar dados do perfil", Toast.LENGTH_SHORT).show();
                    return;
                }

                atualizarCampos(response.body());
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Log.e(TAG, "Falha na conexão: " + t.getMessage());
                Toast.makeText(EditarInformacoes.this,
                        "Falha na conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarCampos(DadosDetalhamentoCliente d) {

        edtNome.setText(safe(d.getNome()));
        edtTelefone.setText(safe(d.getTelefone()));
        edtSexo.setText(safe(d.getSexo()));
        edtIdade.setText(String.valueOf(d.getIdade()));

        edtMedicamentos.setText(safe(d.getMedicamentos()));
        edtEmergencia.setText(safe(d.getContatoEmergencia()));

        txtEmail.setText(safe(d.getEmail()));
        txtCPF.setText(safe(d.getCpf()));

        if (d.getEndereco() != null) {
            edtLogradouro.setText(safe(d.getEndereco().getLogradouro()));
            edtNumero.setText(safe(d.getEndereco().getNumero()));
            edtComplemento.setText(safe(d.getEndereco().getComplemento()));
            edtBairro.setText(safe(d.getEndereco().getBairro()));
            edtCidade.setText(safe(d.getEndereco().getCidade()));
            edtUF.setText(safe(d.getEndereco().getUf()));
            edtCEP.setText(safe(d.getEndereco().getCep()));
        }

        txtEmail.setEnabled(false);
        txtCPF.setEnabled(false);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
    private void salvarDados() {

        Log.d(TAG, "Botão salvar clicado.");

        AtualizarRequest req = new AtualizarRequest();
        req.nome = edtNome.getText().toString().trim();
        req.telefone = edtTelefone.getText().toString().trim();
        req.sexo = edtSexo.getText().toString().trim();

        String idadeStr = edtIdade.getText().toString().trim();
        req.idade = idadeStr.isEmpty() ? null : Integer.parseInt(idadeStr);

        req.medicamentos = edtMedicamentos.getText().toString().trim();
        req.contatoEmergencia = edtEmergencia.getText().toString().trim();

        Endereco end = new Endereco();
        end.setLogradouro(edtLogradouro.getText().toString().trim());
        end.setNumero(edtNumero.getText().toString().trim());
        end.setComplemento(edtComplemento.getText().toString().trim());
        end.setBairro(edtBairro.getText().toString().trim());
        end.setCidade(edtCidade.getText().toString().trim());
        end.setUf(edtUF.getText().toString().trim());
        end.setCep(edtCEP.getText().toString().trim());

        req.endereco = end;

        api.atualizarPerfil(req).enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call, Response<DadosDetalhamentoCliente> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(EditarInformacoes.this, "Erro ao atualizar perfil", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro backend: " + response.code());
                    return;
                }

                Toast.makeText(EditarInformacoes.this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                atualizarCampos(response.body());
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Toast.makeText(EditarInformacoes.this, "Falha: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, t.getMessage());
            }
        });
    }
}
