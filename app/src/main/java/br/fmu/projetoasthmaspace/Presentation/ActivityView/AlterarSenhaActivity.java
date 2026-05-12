package br.fmu.projetoasthmaspace.Presentation.ActivityView;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import br.fmu.projetoasthmaspace.Core.Util.AlterarSenhaRequest;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiClient;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiService;
import br.fmu.projetoasthmaspace.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlterarSenhaActivity extends BaseActivity {

    private EditText etSenhaAtual, etNovaSenha, etConfirmarSenha;
    private boolean visAtual = false, visNova = false, visConfirmar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_senha);

        etSenhaAtual     = findViewById(R.id.etSenhaAtual);
        etNovaSenha      = findViewById(R.id.etNovaSenha);
        etConfirmarSenha = findViewById(R.id.etConfirmarSenha);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancelar).setOnClickListener(v -> finish());

        // Toggles de visibilidade
        ((ImageButton) findViewById(R.id.btnToggleSenhaAtual)).setOnClickListener(v -> {
            visAtual = !visAtual;
            etSenhaAtual.setTransformationMethod(
                    visAtual ? HideReturnsTransformationMethod.getInstance()
                            : PasswordTransformationMethod.getInstance());
            etSenhaAtual.setSelection(etSenhaAtual.length());
            ((ImageButton) v).setImageResource(
                    visAtual ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        });

        ((ImageButton) findViewById(R.id.btnToggleNovaSenha)).setOnClickListener(v -> {
            visNova = !visNova;
            etNovaSenha.setTransformationMethod(
                    visNova ? HideReturnsTransformationMethod.getInstance()
                            : PasswordTransformationMethod.getInstance());
            etNovaSenha.setSelection(etNovaSenha.length());
            ((ImageButton) v).setImageResource(
                    visNova ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        });

        ((ImageButton) findViewById(R.id.btnToggleConfirmarSenha)).setOnClickListener(v -> {
            visConfirmar = !visConfirmar;
            etConfirmarSenha.setTransformationMethod(
                    visConfirmar ? HideReturnsTransformationMethod.getInstance()
                            : PasswordTransformationMethod.getInstance());
            etConfirmarSenha.setSelection(etConfirmarSenha.length());
            ((ImageButton) v).setImageResource(
                    visConfirmar ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        });

        findViewById(R.id.btnSalvarSenha).setOnClickListener(v -> validarESalvar());
    }

    private void validarESalvar() {
        String atual     = etSenhaAtual.getText().toString().trim();
        String nova      = etNovaSenha.getText().toString().trim();
        String confirmar = etConfirmarSenha.getText().toString().trim();

        if (atual.isEmpty() || nova.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!nova.equals(confirmar)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nova.length() < 6) {
            Toast.makeText(this, "Mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        Button btnSalvar = findViewById(R.id.btnSalvarSenha);
        btnSalvar.setEnabled(false);
        btnSalvar.setText("Salvando...");

        ApiClient.getApiService(this)
                .alterarSenha(new AlterarSenhaRequest(atual, nova, confirmar))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        btnSalvar.setEnabled(true);
                        btnSalvar.setText("Salvar senha");
                        if (response.isSuccessful()) {
                            Toast.makeText(AlterarSenhaActivity.this,
                                    "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else if (response.code() == 400) {
                            Toast.makeText(AlterarSenhaActivity.this,
                                    "Senha atual incorreta", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AlterarSenhaActivity.this,
                                    "Erro ao alterar senha", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        btnSalvar.setEnabled(true);
                        btnSalvar.setText("Salvar senha");
                        Toast.makeText(AlterarSenhaActivity.this,
                                "Erro de conexão", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}