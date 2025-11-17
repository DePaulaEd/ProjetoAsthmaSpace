package br.fmu.projetoasthmaspace;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.fmu.projetoasthmaspace.databinding.ActivityLoginBinding;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lógica para criar o link no texto "Cadastre-se!"
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
}