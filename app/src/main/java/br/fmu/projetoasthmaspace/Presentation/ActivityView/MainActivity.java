package br.fmu.projetoasthmaspace.Presentation.ActivityView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.fmu.projetoasthmaspace.Core.Session.UserServiceHelper;
import br.fmu.projetoasthmaspace.Core.Domain.Cliente.DadosDetalhamentoCliente;
import br.fmu.projetoasthmaspace.Data.worker.NotificacaoScheduler;
import br.fmu.projetoasthmaspace.Data.worker.QualidadeArWorker;
import br.fmu.projetoasthmaspace.Core.Session.UserSessionManager;
import br.fmu.projetoasthmaspace.Presentation.Fragment.DiarioSintomasFragment;
import br.fmu.projetoasthmaspace.Presentation.Fragment.EducativoFragment;
import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiClient;
import br.fmu.projetoasthmaspace.databinding.ActivityMainBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
//        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Validação do token ao abrir o app
        UserSessionManager session = new UserSessionManager(this);
        String token = session.getToken();

        if (token == null || token.isEmpty()) {
            redirecionarParaLogin();
            return;
        }

        ApiClient.getApiService(this).getMeuPerfil().enqueue(new Callback<DadosDetalhamentoCliente>() {
            @Override
            public void onResponse(Call<DadosDetalhamentoCliente> call, Response<DadosDetalhamentoCliente> response) {
                if (!response.isSuccessful()) {
                    session.clear();
                    redirecionarParaLogin();
                }
            }

            @Override
            public void onFailure(Call<DadosDetalhamentoCliente> call, Throwable t) {
                Log.w("MainActivity", "Sem conexão para validar token: " + t.getMessage());
            }
        });

        if (listaDeLembretes.isEmpty()) {
            preencherLembretesExemplo();
        }

//        Toolbar toolbar = binding.toolbar;
//        setSupportActionBar(toolbar);
//
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
//        }

        binding.toolbarPerfilContainer.setOnClickListener(v -> {
            Log.d("MainActivity", "perfil clicado");
            replacefragment(new PerfilActivity());
            binding.bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < binding.bottomNavigationView.getMenu().size(); i++) {
                binding.bottomNavigationView.getMenu().getItem(i).setChecked(false);
            }
            binding.bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
        });

        replacefragment(new TelaInicialActivity());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                replacefragment(new TelaInicialActivity());
            } else if (itemId == R.id.navigation_lembretes) {
                replacefragment(new LembretesActivity());
            } else if (itemId == R.id.navigation_tarefas) {
                replacefragment(new TarefasActivity());
            } else if (itemId == R.id.navigation_diario) {
                replacefragment(new DiarioSintomasFragment());
            } else if (itemId == R.id.navigation_educativo) {
                replacefragment(new EducativoFragment());
            }
            return true;
        });

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        configurarImmersiveMode();
        carregarNomeUsuario();
        criarCanal();
//        WorkManager.getInstance(this).enqueue(
//                new androidx.work.OneTimeWorkRequest.Builder(QualidadeArWorker.class).build()
//        );
        NotificacaoScheduler.agendarVerificacaoAr(this);


    }

    private void carregarNomeUsuario() {
        UserSessionManager session = new UserSessionManager(this);
        String nomeCompleto = session.getNome();
        String token = session.getToken();
        Log.d("MainActivity", "getNome retornou: " + nomeCompleto);

        if (nomeCompleto != null && !nomeCompleto.trim().isEmpty()) {
            String primeiroNome = nomeCompleto.split(" ")[0];
            binding.toolbarUserName.setText("Olá, " + primeiroNome);
            binding.toolbarUserName.setVisibility(View.VISIBLE);

        }  else if (token != null) {
        UserServiceHelper.buscarNomeUsuario(
                this,
                new UserServiceHelper.NomeCallback() {
                    @Override
                    public void onSuccess(String nome) {
                        Log.d("MainActivity", "buscarNome onSuccess: " + nome);
                        session.saveNome(nome);
                        runOnUiThread(() -> {
                            String primeiroNome = nome.split(" ")[0];
                            binding.toolbarUserName.setText("Olá, " + primeiroNome);
                            binding.toolbarUserName.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onError(String erro) {
                        Log.e("MainActivity", "buscarNome onError: " + erro);
                        runOnUiThread(() ->
                                binding.toolbarUserName.setVisibility(View.GONE)
                        );
                    }
                });
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

    private void redirecionarParaLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void configurarImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            // Android 10 e abaixo
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }



}
