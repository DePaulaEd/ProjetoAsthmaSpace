package br.fmu.projetoasthmaspace.ActivityView;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import br.fmu.projetoasthmaspace.databinding.ActivityAjudaBinding;

public class AjudaActivity extends AppCompatActivity {

    private ActivityAjudaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAjudaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnVoltar.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

