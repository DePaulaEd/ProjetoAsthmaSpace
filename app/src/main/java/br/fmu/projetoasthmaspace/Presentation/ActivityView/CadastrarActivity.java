package br.fmu.projetoasthmaspace.Presentation.ActivityView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.databinding.ActivityCadastrarBinding;

public class CadastrarActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ActivityCadastrarBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCadastrarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.spinnerSexo.setOnItemSelectedListener(this);

        binding.btnVoltar.setOnClickListener(v -> {
            startActivity(new Intent(CadastrarActivity.this, LoginActivity.class));
            finish();
        });

        configurarMascaraData();

        binding.btnContinuar.setOnClickListener(v -> {
            String nomeCompleto    = binding.editTextNomeCompleto.getText().toString();
            String email           = binding.editTextEmail.getText().toString();
            String senha           = binding.editTextCriarSenha.getText().toString();
            String cpf             = binding.editTextCpf.getText().toString();
            String telefone        = binding.editTextTelefone.getText().toString();
            String dataNascimento  = binding.editTextDataNascimento.getText().toString();
            String sexo            = binding.spinnerSexo.getSelectedItem().toString();

            if (nomeCompleto.isEmpty() || email.isEmpty() || senha.isEmpty() ||
                    dataNascimento.length() < 10 || sexo.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios com *.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sexo.equals("Selecione")) {
                Toast.makeText(this, "Selecione um sexo.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Converte DD/MM/AAAA → yyyy-MM-dd para o backend
            String dataFormatada;
            try {
                String[] partes = dataNascimento.split("/");
                dataFormatada = partes[2] + "-" + partes[1] + "-" + partes[0];
            } catch (Exception e) {
                Toast.makeText(this, "Data de nascimento inválida.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(CadastrarActivity.this, InfAdicionaisActivity.class);
            intent.putExtra("USER_NAME", nomeCompleto);
            intent.putExtra("email", email);
            intent.putExtra("cpf", cpf);
            intent.putExtra("telefone", telefone);
            intent.putExtra("dataNascimento", dataFormatada); // yyyy-MM-dd
            intent.putExtra("senha", senha);
            intent.putExtra("sexo", sexo);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void configurarMascaraData() {
        binding.editTextDataNascimento.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                String digits = s.toString().replaceAll("[^0-9]", "");
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length() && i < 8; i++) {
                    if (i == 2 || i == 4) formatted.append("/");
                    formatted.append(digits.charAt(i));
                }
                isUpdating = true;
                binding.editTextDataNascimento.setText(formatted.toString());
                binding.editTextDataNascimento.setSelection(formatted.length());
                isUpdating = false;
            }
        });
    }

    @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}
    @Override public void onNothingSelected(AdapterView<?> parent) {}
}