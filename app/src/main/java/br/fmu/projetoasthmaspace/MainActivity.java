package br.fmu.projetoasthmaspace;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.fmu.projetoasthmaspace.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    public static class Lembrete {
        public String titulo;
        public String horario;
        public Date data;
        public boolean concluida;

        public Lembrete(String titulo, String horario, Date data) {
            this.titulo = titulo;
            this.horario = horario;
            this.data = data;
            this.concluida = false;
        }
    }

    public static List<Lembrete> listaDeLembretes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (listaDeLembretes.isEmpty()) {
            preencherLembretesExemplo();
        }

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        replacefragment(new TelaInicial());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                replacefragment(new TelaInicial());
            } else if (itemId == R.id.navigation_lembretes) {
                replacefragment(new Lembretes());
            } else if (itemId == R.id.navigation_tarefas) {
                replacefragment(new Tarefas());
            } else if (itemId == R.id.navigation_diario) {
                replacefragment(new DiarioSintomas());
            } else if (itemId == R.id.navigation_educativo) {
                replacefragment(new Educativo());
            }
            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void preencherLembretesExemplo() {
        // Exemplos para Hoje
        Date hoje = new Date();
        listaDeLembretes.add(new Lembrete("Usar Inalador Preventivo", "08:00", hoje));
        listaDeLembretes.add(new Lembrete("Medir pico de fluxo", "08:15", hoje));
        listaDeLembretes.add(new Lembrete("Tomar antialérgico", "12:00", hoje));
        listaDeLembretes.add(new Lembrete("Usar Inalador Preventivo", "20:00", hoje));

        // Exemplos para Ontem (dia 16)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date ontem = cal.getTime();

        Lembrete lembreteOntem1 = new Lembrete("Tomar Comprimido", "22:00", ontem);
        lembreteOntem1.concluida = true; // Marcar como concluído
        listaDeLembretes.add(lembreteOntem1);

        Lembrete lembreteOntem2 = new Lembrete("Usar Inalador de Alívio", "15:30", ontem);
        lembreteOntem2.concluida = true; // Marcar como concluído
        listaDeLembretes.add(lembreteOntem2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_perfil) {
            replacefragment(new Perfil());
            binding.bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < binding.bottomNavigationView.getMenu().size(); i++) {
                binding.bottomNavigationView.getMenu().getItem(i).setChecked(false);
            }
            binding.bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void replacefragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}
