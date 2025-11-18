package br.fmu.projetoasthmaspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.fmu.projetoasthmaspace.databinding.ActivityCadastrarBinding;

public class Cadastrar extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ActivityCadastrarBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCadastrarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.spinnerSexo.setOnItemSelectedListener(this);

        binding.btnContinuar.setOnClickListener(v -> {
            String nomeCompleto = binding.editTextNomeCompleto.getText().toString();

            if (nomeCompleto.trim().isEmpty()) {
                Toast.makeText(this, "Por favor, insira seu nome completo.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Cadastrar.this, InfAdicionais.class);
            // Passa o nome para a próxima tela
            intent.putExtra("USER_NAME", nomeCompleto);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Lógica para quando um item do spinner é selecionado
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Lógica para quando nada é selecionado
    }
}
