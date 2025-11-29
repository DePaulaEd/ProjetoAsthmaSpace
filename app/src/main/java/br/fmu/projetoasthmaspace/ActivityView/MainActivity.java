package br.fmu.projetoasthmaspace.ActivityView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
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

import br.fmu.projetoasthmaspace.R;
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

        binding.toolbarPerfilContainer.setOnClickListener(v -> {
            replacefragment(new Perfil());
            binding.bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < binding.bottomNavigationView.getMenu().size(); i++) {
                binding.bottomNavigationView.getMenu().getItem(i).setChecked(false);
            }
            binding.bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
        });

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

        carregarNomeUsuario();
        criarCanal();


    }

    private void carregarNomeUsuario() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String nomeCompleto = prefs.getString("user_name", null); // Pega o nome, ou null se não houver

        if (nomeCompleto != null && !nomeCompleto.trim().isEmpty()) {
            String primeiroNome = nomeCompleto.split(" ")[0];
            binding.toolbarUserName.setText("Olá, " + primeiroNome);
            binding.toolbarUserName.setVisibility(View.VISIBLE);
        } else {
            // Se não houver nome salvo, esconde o TextView do nome
            binding.toolbarUserName.setVisibility(View.GONE);
        }
    }

    private void preencherLembretesExemplo() {
        Date hoje = new Date();
        listaDeLembretes.add(new Lembrete("Usar Inalador Preventivo", "08:00", hoje));
        listaDeLembretes.add(new Lembrete("Medir pico de fluxo", "08:15", hoje));
        listaDeLembretes.add(new Lembrete("Tomar antialérgico", "12:00", hoje));
        listaDeLembretes.add(new Lembrete("Usar Inalador Preventivo", "20:00", hoje));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date ontem = cal.getTime();

        Lembrete lembreteOntem1 = new Lembrete("Tomar Comprimido", "22:00", ontem);
        lembreteOntem1.concluida = true;
        listaDeLembretes.add(lembreteOntem1);

        Lembrete lembreteOntem2 = new Lembrete("Usar Inalador de Alívio", "15:30", ontem);
        lembreteOntem2.concluida = true;
        listaDeLembretes.add(lembreteOntem2);
    }

    private void replacefragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
    private void criarCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    "LEMBRETES",
                    "Lembretes do App",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(canal);
        }
    }

}
