package br.fmu.projetoasthmaspace.Domain;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import br.fmu.projetoasthmaspace.ActivityView.Login;
import br.fmu.projetoasthmaspace.ActivityView.TelaInicial;

public class AppStart extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = getSharedPreferences("APP", MODE_PRIVATE)
                .getString("TOKEN", null);

        if (token == null) {
            // Usuário não logado → vai para Login
            startActivity(new Intent(this, Login.class));
        } else {
            // Usuário logado → vai para tela inicial
            startActivity(new Intent(this, TelaInicial.class));
        }

        finish();
    }
}
