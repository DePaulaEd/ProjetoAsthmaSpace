package br.fmu.projetoasthmaspace.ActivityView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.fmu.projetoasthmaspace.Domain.LoginRequest;
import br.fmu.projetoasthmaspace.Domain.SharedPreferencesKeys;
import br.fmu.projetoasthmaspace.Domain.TokenResponse;
import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.Service.ApiClient;
import br.fmu.projetoasthmaspace.Service.ApiService;
import br.fmu.projetoasthmaspace.databinding.ActivityLoginBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.btnEntrar.setOnClickListener(v -> fazerLogin());

        // Link "Cadastre-se!"
        String textoCompleto = getString(R.string.nao_tem_conta_cadastre_se);
        SpannableString spannableString = new SpannableString(textoCompleto);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Login.this, Cadastrar.class);
                startActivity(intent);
            }
        };

        String textoLink = "Cadastre-se!";
        int inicioDoLink = textoCompleto.indexOf(textoLink);
        int fimDoLink = inicioDoLink + textoLink.length();

        if (inicioDoLink != -1) {
            spannableString.setSpan(clickableSpan, inicioDoLink, fimDoLink, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new UnderlineSpan(), inicioDoLink, fimDoLink, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        binding.textCadastre.setText(spannableString);
        binding.textCadastre.setMovementMethod(LinkMovementMethod.getInstance());

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

        ApiService api = ApiClient.getApiService();

        api.login(req).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String token = response.body().token;
                    Log.d("AUTH_TOKEN", "Token Recebido e Salvo: " + token);


                    SharedPreferences prefs = getSharedPreferences(SharedPreferencesKeys.PREFS_FILE_NAME, MODE_PRIVATE);
                    prefs.edit().putString(SharedPreferencesKeys.TOKEN_KEY, token).apply();
                    Log.d("AUTH_TOKEN", "Token Salvo: " + token);



                    Toast.makeText(Login.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(Login.this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Toast.makeText(Login.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}