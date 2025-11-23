package br.fmu.projetoasthmaspace;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import br.fmu.projetoasthmaspace.Domain.Lembrete;
import br.fmu.projetoasthmaspace.Domain.LembreteReceiver;
import br.fmu.projetoasthmaspace.Domain.LembreteRequest;
import br.fmu.projetoasthmaspace.Domain.LembreteResponse;
import br.fmu.projetoasthmaspace.Service.ApiClient;
import br.fmu.projetoasthmaspace.Service.ApiService;
import br.fmu.projetoasthmaspace.databinding.ActivityLembretesBinding;
import br.fmu.projetoasthmaspace.databinding.CardLembreteStatBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Lembretes extends Fragment {

    private ActivityLembretesBinding binding;
    private String token;
    private ApiService api;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Lista local de lembretes
    private final List<Lembrete> listaDeLembretes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityLembretesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("APP", Context.MODE_PRIVATE);
        token = prefs.getString("TOKEN", null);

        api = ApiClient.getApiService(token);

        binding.fabNovoLembrete.setOnClickListener(v -> showNovoLembreteDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarLembretesDoBackend();
    }

    private void carregarLembretesDoBackend() {
        api.listarLembretes().enqueue(new Callback<List<LembreteResponse>>() {
            @Override
            public void onResponse(Call<List<LembreteResponse>> call, Response<List<LembreteResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Parsing em background
                    new Thread(() -> {
                        List<Lembrete> tempLista = new ArrayList<>();
                        for (LembreteResponse resp : response.body()) {
                            try {
                                // Aqui tratamos a data nula ou vazia
                                String dataStr = resp.getData();
                                if (dataStr == null || dataStr.isEmpty()) {
                                    dataStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                    Log.w("Lembretes", "Data nula substituída para lembrete id: " + resp.getId());
                                }

                                Date dataParsed = dateFormat.parse(dataStr);

                                Lembrete l = new Lembrete(
                                        resp.getId(),
                                        resp.getTitulo(),
                                        "", // descricao vazia
                                        dataStr,  // agora garantido não nulo
                                        resp.getHorario(),
                                        resp.isConcluido()
                                );
                                tempLista.add(l);
                            } catch (ParseException e) {
                                Log.e("Lembretes", "Erro ao parsear data do lembrete id: " + resp.getId(), e);
                            }
                        }


                        // Atualiza UI na thread principal
                        requireActivity().runOnUiThread(() -> {
                            listaDeLembretes.clear();
                            listaDeLembretes.addAll(tempLista);
                            atualizarPainelDeResumo();
                            atualizarListaDeLembretes();
                        });

                    }).start();


                } else {
                    Toast.makeText(getContext(), "Erro ao buscar lembretes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LembreteResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarPainelDeResumo() {
        long hojeCount = listaDeLembretes.stream().filter(l -> isToday(l.getData())).count();
        long programadosCount = listaDeLembretes.stream().filter(l -> isToday(l.getData()) && !l.isAtivo()).count();
        long todosCount = listaDeLembretes.size();
        long concluidosCount = listaDeLembretes.stream().filter(Lembrete::isAtivo).count();

        setupCard(binding.statHoje, "Hoje", String.valueOf(hojeCount), R.drawable.icon_hoje, R.drawable.bg_icon_hoje, R.color.blue_navy);
        setupCard(binding.statProgramados, "Programados", String.valueOf(programadosCount), R.drawable.icon_programados, R.drawable.bg_icon_programados, R.color.red_dark);
        setupCard(binding.statTodos, "Todos", String.valueOf(todosCount), R.drawable.icon_todos, R.drawable.bg_icon_todos, R.color.gray_dark);
        setupCard(binding.statConcluidos, "Concluídos", String.valueOf(concluidosCount), R.drawable.icon_concluidos, R.drawable.bg_icon_concluidos, R.color.green_dark);
    }

    private void setupCard(CardLembreteStatBinding cardBinding, String title, String count, int iconRes, int bgRes, int tintColorRes) {
        cardBinding.statTitle.setText(title);
        cardBinding.statCount.setText(count);
        cardBinding.statIcon.setImageResource(iconRes);
        cardBinding.statIcon.setBackgroundResource(bgRes);
        cardBinding.statIcon.setColorFilter(ContextCompat.getColor(getContext(), tintColorRes), PorterDuff.Mode.SRC_IN);
    }

    private boolean isToday(String dataStr) {
        if (dataStr == null || dataStr.isEmpty()) return false;
        try {
            Date date = dateFormat.parse(dataStr);
            Calendar hoje = Calendar.getInstance();
            Calendar data = Calendar.getInstance();
            data.setTime(date);
            return hoje.get(Calendar.YEAR) == data.get(Calendar.YEAR) &&
                    hoje.get(Calendar.DAY_OF_YEAR) == data.get(Calendar.DAY_OF_YEAR);
        } catch (ParseException e) {
            Log.e("Lembretes", "Erro ao parsear data: " + dataStr, e);
            return false;
        }
    }


    private void showNovoLembreteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_novo_lembrete, null);

        EditText tituloInput = dialogView.findViewById(R.id.edit_text_titulo_lembrete);
        EditText horarioInput = dialogView.findViewById(R.id.edit_text_horario_lembrete);

        builder.setView(dialogView);

        builder.setPositiveButton("Salvar", (dialog, id) -> {
            String titulo = tituloInput.getText().toString().trim();
            String horario = horarioInput.getText().toString().trim();

            if (titulo.isEmpty() || horario.isEmpty()) {
                Toast.makeText(getContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            String dataHoje = dateFormat.format(new Date());
            LembreteRequest req = new LembreteRequest(titulo, dataHoje, horario);

            api.registrarLembrete(req).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Lembrete registrado!", Toast.LENGTH_SHORT).show();

                        String[] partes = horario.split(":");
                        int hora = Integer.parseInt(partes[0]);
                        int minuto = Integer.parseInt(partes[1]);

                        agendarLembrete(hora, minuto, titulo, "Lembrete programado!");
                        carregarLembretesDoBackend();
                    } else {
                        Toast.makeText(getContext(), "Erro ao salvar!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Falha na conexão!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancelar", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void atualizarListaDeLembretes() {
        if (binding == null) return;
        binding.containerLembretes.removeAllViews();

        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault());

        // Agrupar lembretes por data formatada
        Map<String, List<Lembrete>> lembretesAgrupados =
                listaDeLembretes.stream()
                        .sorted(Comparator.comparing(Lembrete::getData).reversed())
                        .collect(Collectors.groupingBy(l -> {
                            String dataStr = l.getData();
                            if (dataStr == null || dataStr.isEmpty()) {
                                dataStr = parseFormat.format(new Date()); // fallback para data atual
                            }
                            try {
                                Date date = parseFormat.parse(dataStr);
                                return displayFormat.format(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return dataStr; // fallback caso parsing falhe
                            }
                        }));

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Map.Entry<String, List<Lembrete>> entry : lembretesAgrupados.entrySet()) {
            View groupView = inflater.inflate(R.layout.item_data_expandable, binding.containerLembretes, false);

            TextView dataHeader = groupView.findViewById(R.id.text_data_header);
            LinearLayout containerLembretesDoDia = groupView.findViewById(R.id.container_lembretes_do_dia);

            dataHeader.setText(entry.getKey());

            List<Lembrete> lembretesDoDia = entry.getValue();
            lembretesDoDia.sort(Comparator.comparing(Lembrete::getHorario));

            for (Lembrete lembrete : lembretesDoDia) {
                TextView lembreteView = new TextView(getContext());
                String status = lembrete.isAtivo() ? "(Concluído)" : "(Programado)";
                String texto = String.format(Locale.getDefault(), "%s - %s %s",
                        lembrete.getHorario(), lembrete.getTitulo(), status);

                lembreteView.setText(texto);
                lembreteView.setTextColor(getResources().getColor(R.color.white));
                lembreteView.setTextSize(16);
                lembreteView.setPadding(16, 8, 16, 8);

                containerLembretesDoDia.addView(lembreteView);
            }

            dataHeader.setOnClickListener(v -> containerLembretesDoDia.setVisibility(
                    containerLembretesDoDia.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            ));

            binding.containerLembretes.addView(groupView);
        }
    }


    private void agendarLembrete(int hora, int minuto, String titulo, String mensagem) {
        // 1️⃣ Cria o horário do lembrete
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hora);
        c.set(Calendar.MINUTE, minuto);
        c.set(Calendar.SECOND, 0);

        // Se o horário já passou, agenda para o dia seguinte
        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 2️⃣ Cria o Intent para o BroadcastReceiver
        Intent intent = new Intent(getContext(), LembreteReceiver.class);
        intent.putExtra("titulo", titulo);
        intent.putExtra("mensagem", mensagem);

        // 3️⃣ Cria PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                (int) System.currentTimeMillis(), // ID único
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 4️⃣ Pega AlarmManager
        AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            // 5️⃣ Verifica se pode usar alarmes exatos em Android 12+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            c.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    // Fallback: alarme aproximado
                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            c.getTimeInMillis(),
                            pendingIntent
                    );
                    Toast.makeText(getContext(), "Permissão de alarmes exatos necessária. Agendado como aproximado.", Toast.LENGTH_LONG).show();
                }
            } else {
                // Android < 12: pode usar normalmente
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        c.getTimeInMillis(),
                        pendingIntent
                );
            }

            Toast.makeText(getContext(), "Lembrete agendado!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Não foi possível acessar AlarmManager.", Toast.LENGTH_SHORT).show();
        }

        // 6️⃣ Opcional: envia para o backend
        enviarLembreteParaBackend(titulo, c, mensagem);
    }

    private void enviarLembreteParaBackend(String titulo, Calendar dataHora, String mensagem) {
        String data = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dataHora.getTime());
        String horario = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(dataHora.getTime());

        LembreteRequest req = new LembreteRequest(titulo, data, horario);

        api.registrarLembrete(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Lembretes", "Lembrete registrado no backend com sucesso!");
                } else {
                    Log.w("Lembretes", "Falha ao registrar lembrete no backend");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Lembretes", "Erro de conexão ao registrar lembrete", t);
            }
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
