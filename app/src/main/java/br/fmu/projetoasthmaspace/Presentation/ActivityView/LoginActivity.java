package br.fmu.projetoasthmaspace.Presentation.ActivityView;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.fmu.projetoasthmaspace.Core.Domain.Log.LoginRequest;
import br.fmu.projetoasthmaspace.Core.Domain.Log.TokenResponse;
import br.fmu.projetoasthmaspace.Core.Session.UserSessionManager;
import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiClient;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiService;
import br.fmu.projetoasthmaspace.databinding.ActivityLoginBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.btnEntrar.setOnClickListener(v -> fazerLogin());


        binding.btnCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastrarActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void fazerLogin() {
        String email = binding.editTextEmail.getText().toString();
        String senha = binding.editTextSenha.getText().toString();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest req = new LoginRequest(email, senha);

        api = ApiClient.getApiService(getApplicationContext());


        api.login(req).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String token = response.body().token;

                    UserSessionManager session = new UserSessionManager(LoginActivity.this);
                    session.clear();
                    session.saveToken(token);

                    api.getMeuId().enqueue(new Callback<Long>() {
                        @Override
                        public void onResponse(Call<Long> call, Response<Long> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                session.saveClienteId(response.body());
                            }
                        }
                        @Override
                        public void onFailure(Call<Long> call, Throwable t) {}
                    });


                    Toast.makeText(LoginActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(LoginActivity.this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}