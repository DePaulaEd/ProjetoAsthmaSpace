package br.fmu.projetoasthmaspace;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import br.fmu.projetoasthmaspace.databinding.ActivityInformacoesPessoaisBinding;

public class InformacoesPessoais extends AppCompatActivity {

    private ActivityInformacoesPessoaisBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInformacoesPessoaisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        carregarDados();
    }

    private void carregarDados() {

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        String nome = prefs.getString("user_name", "---");
        String email = prefs.getString("user_email", "---");
        String telefone = prefs.getString("user_phone", "---");
        String cpf = prefs.getString("user_cpf", "---");
        String sexo = prefs.getString("user_sexo", "---");
        String dataNascimento = prefs.getString("user_birth", "---");

        String medicamentos = prefs.getString("user_medicamentos", "---");
        String respiratorios = prefs.getString("user_problemas_respiratorios", "---");
        String emergencia = prefs.getString("user_contato_emergencia", "---");

        binding.txtNome.setText(nome);
        binding.txtEmail.setText(email);
        binding.txtTelefone.setText(telefone);
        binding.txtCPF.setText(cpf);
        binding.txtSexo.setText(sexo);
        binding.txtDataNascimento.setText(dataNascimento);
        binding.txtMedicamentos.setText(medicamentos);
        binding.txtRespiratorios.setText(respiratorios);
        binding.txtEmergencia.setText(emergencia);
    }
}
