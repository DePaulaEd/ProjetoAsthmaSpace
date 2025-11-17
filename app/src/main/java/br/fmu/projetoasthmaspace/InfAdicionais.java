package br.fmu.projetoasthmaspace;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.fmu.projetoasthmaspace.databinding.ActivityInfAdicionaisBinding;

public class InfAdicionais extends AppCompatActivity {

    private ActivityInfAdicionaisBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityInfAdicionaisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnConcluir.setOnClickListener(v -> {
            Intent intent = new Intent(InfAdicionais.this, MainActivity.class);
            // Limpa o histórico de navegação para que o usuário não possa voltar para as telas de cadastro
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}