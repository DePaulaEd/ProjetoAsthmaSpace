package br.fmu.projetoasthmaspace.ActivityView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import br.fmu.projetoasthmaspace.Domain.Lembrete;
import br.fmu.projetoasthmaspace.Domain.LembreteReceiver;
import br.fmu.projetoasthmaspace.Domain.LembreteRequest;
import br.fmu.projetoasthmaspace.Domain.LembreteResponse;
import br.fmu.projetoasthmaspace.Domain.LembreteUpdateRequest;
import br.fmu.projetoasthmaspace.Domain.UserSessionManager;
import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.Service.ApiClient;
import br.fmu.projetoasthmaspace.Service.ApiService;
import br.fmu.projetoasthmaspace.databinding.ActivityLembretesBinding;
import br.fmu.projetoasthmaspace.databinding.CardLembreteStatBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Lembretes extends Fragment {

    private ActivityLembretesBinding binding;
    private boolean isEditMode = false;
    private String token;
    private ApiService api;
    private final SimpleDateFormat parseDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault());


    // Lista local de lembretes, agora tipada corretamente com a classe Domain.Lembrete
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
        api = ApiClient.getApiService(requireContext());

        Log.d("LEMBRETES_TOKEN", "Token Carregado: " + token);
        binding.fabNovoLembrete.setOnClickListener(v -> showLembreteDialog(null));

        binding.fabEditarLembrete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = !isEditMode;
                toggleUiForEditMode();
                atualizarListaDeLembretes();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarLembretesDoBackend();
    }

    private void toggleUiForEditMode() {
        if (isEditMode) {
            binding.fabEditarLembrete.setText("Concluir");
            binding.fabNovoLembrete.setVisibility(View.GONE);
        } else {
            binding.fabEditarLembrete.setText("Editar");
            binding.fabNovoLembrete.setVisibility(View.VISIBLE);
        }
    }

    // --- Funções de Backend ---

    private void carregarLembretesDoBackend() {
        api.listarLembretes().enqueue(new Callback<List<LembreteResponse>>() {
            @Override
            public void onResponse(Call<List<LembreteResponse>> call, Response<List<LembreteResponse>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        List<Lembrete> tempLista = new ArrayList<>();
                        for (LembreteResponse resp : response.body()) {
                            try {
                                String dataStr = resp.getData();
                                if (dataStr == null || dataStr.isEmpty()) {
                                    dataStr = parseDateFormat.format(new Date());
                                }
                                Date dataParsed = parseDateFormat.parse(dataStr);

                                Lembrete l = new Lembrete(
                                        resp.getId(),
                                        resp.getTitulo(),
                                        "", // Descrição não usada
                                        dataStr,
                                        resp.getHorario(),
                                        resp.isConcluido()
                                );
                                tempLista.add(l);
                            } catch (ParseException e) {
                                Log.e("Lembretes", "Erro ao parsear data do lembrete id: " + resp.getId(), e);
                            }
                        }
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

    private void registrarLembrete(LembreteRequest req, int hora, int minuto, String titulo) {
        api.registrarLembrete(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Lembrete salvo!", Toast.LENGTH_SHORT).show();
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
    }

    // No Fragment Lembretes.java:
    private void atualizarLembrete(Lembrete lembrete, String novoTitulo, String novoHorario) {

        UserSessionManager session = new UserSessionManager(getContext());
        String currentToken = session.getToken();
        if (currentToken == null) {
            Toast.makeText(getContext(), "Erro: Token inválido.", Toast.LENGTH_LONG).show();
            return;
        }

        api = ApiClient.getApiService(requireContext());


        LembreteUpdateRequest req = new LembreteUpdateRequest(
                novoTitulo,
                lembrete.getData(),
                novoHorario,
                lembrete.isAtivo()
        );

        api.atualizarDados(lembrete.getId(), req)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Lembrete atualizado!", Toast.LENGTH_SHORT).show();
                            carregarLembretesDoBackend();
                        } else {
                            Toast.makeText(getContext(),
                                    "Erro ao atualizar (" + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(),
                                "Falha de conexão!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void mostrarDialogoExcluir(Long id) {
        if (id == null) {
            Log.e("DELETE", "ID DO LEMBRETE É NULO!");
            return;
        }

        Context ctx = getContext();
        if (ctx == null) return;

        new AlertDialog.Builder(ctx)
                .setTitle("Excluir Lembrete")
                .setMessage("Tem certeza que deseja excluir este lembrete?")
                .setPositiveButton("Excluir", (dialog, which) -> deletarLembrete(id))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletarLembrete(Long id) {
        Context ctx = getContext();
        if (ctx == null) return;

        UserSessionManager session = new UserSessionManager(ctx);
        String token = session.getToken();
        if (token == null) {
            Toast.makeText(ctx, "Erro: Token inválido.", Toast.LENGTH_LONG).show();
            return;
        }

        api = ApiClient.getApiService(requireContext());


        api.deletarLembrete(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Lembrete excluído!", Toast.LENGTH_SHORT).show();
                    carregarLembretesDoBackend();
                } else {
                    Toast.makeText(getContext(),
                            "Erro ao excluir (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(),
                        "Falha de conexão!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }









    private void showLembreteDialog(@Nullable final Lembrete lembreteExistente) {
        final boolean isEditing = lembreteExistente != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_novo_lembrete, null);
        final EditText tituloInput = dialogView.findViewById(R.id.edit_text_titulo_lembrete);
        final EditText horarioInput = dialogView.findViewById(R.id.edit_text_horario_lembrete);

        TextView dialogTitle = new TextView(getContext());
        dialogTitle.setPadding(60, 40, 60, 20);
        dialogTitle.setTextSize(20);
        dialogTitle.setTextColor(Color.WHITE);

        if (isEditing) {
            dialogTitle.setText("Editar Lembrete");
            tituloInput.setText(lembreteExistente.getTitulo());
            horarioInput.setText(lembreteExistente.getHorario());
        } else {
            dialogTitle.setText("Novo Lembrete");
        }
        builder.setCustomTitle(dialogTitle);

        builder.setView(dialogView);
        builder.setPositiveButton("Salvar", (dialog, id) -> {
            String titulo = tituloInput.getText().toString().trim();
            String horario = horarioInput.getText().toString().trim();

            if (titulo.isEmpty() || horario.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String[] partes = horario.split(":");
                int hora = Integer.parseInt(partes[0]);
                int minuto = Integer.parseInt(partes[1]);

                if (isEditing) {
                    atualizarLembrete(lembreteExistente, titulo, horario);
                } else {
                    String dataHoje = parseDateFormat.format(new Date());
                    LembreteRequest req = new LembreteRequest(titulo, dataHoje, horario);
                    registrarLembrete(req, hora, minuto, titulo);
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                Toast.makeText(getContext(), "Formato de horário inválido (Use HH:mm).", Toast.LENGTH_SHORT).show();
            }

        });
        builder.setNegativeButton("Cancelar", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void atualizarListaDeLembretes() {
        if (binding == null) return;
        binding.containerLembretes.removeAllViews();

        // 1. Agrupar lembretes por data
        Map<String, List<Lembrete>> lembretesAgrupados = new HashMap<>();
        for (Lembrete lembrete : listaDeLembretes) {
            try {
                Date date = parseDateFormat.parse(lembrete.getData());
                String dataFormatada = displayDateFormat.format(date);
                if (!lembretesAgrupados.containsKey(dataFormatada)) {
                    lembretesAgrupados.put(dataFormatada, new ArrayList<Lembrete>());
                }
                lembretesAgrupados.get(dataFormatada).add(lembrete);
            } catch (ParseException e) {
                Log.e("Lembretes", "Erro ao parsear data: " + lembrete.getData(), e);
            }
        }

        // 2. Ordenar as datas (Mais recentes primeiro)
        List<String> datasOrdenadas = new ArrayList<>(lembretesAgrupados.keySet());
        Collections.sort(datasOrdenadas, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                try {
                    Date d1 = displayDateFormat.parse(s1);
                    Date d2 = displayDateFormat.parse(s2);
                    return d2.compareTo(d1); // Ordem decrescente
                } catch (ParseException e) {
                    return 0;
                }
            }
        });

        LayoutInflater inflater = LayoutInflater.from(getContext());

        // 3. Renderizar grupos e itens
        for (String dataKey : datasOrdenadas) {
            final List<Lembrete> lembretesDoDia = lembretesAgrupados.get(dataKey);

            View groupView = inflater.inflate(R.layout.item_data_expandable, binding.containerLembretes, false);
            TextView dataHeader = groupView.findViewById(R.id.text_data_header);
            final ImageView deleteGroupIcon = groupView.findViewById(R.id.icon_delete_group);
            final LinearLayout containerLembretesDoDia = groupView.findViewById(R.id.container_lembretes_do_dia);

            dataHeader.setText(dataKey);


            lembretesDoDia.sort(Comparator.comparing(Lembrete::getHorario));


            if (isEditMode) {
                deleteGroupIcon.setVisibility(View.GONE); // Desativamos a exclusão de grupo para simplificar o backend
            } else {
                deleteGroupIcon.setVisibility(View.GONE);
            }

            containerLembretesDoDia.removeAllViews();
            for (final Lembrete lembrete : lembretesDoDia) {
                View lembreteItemView = inflater.inflate(R.layout.item_lembrete_edit, containerLembretesDoDia, false);
                TextView lembreteText = lembreteItemView.findViewById(R.id.text_lembrete);
                ImageView deleteItemIcon = lembreteItemView.findViewById(R.id.icon_delete_item);

                String status = lembrete.isAtivo() ? "(Concluído)" : "(Programado)";
                String textoLembrete = String.format(Locale.getDefault(), "%s - %s %s", lembrete.getHorario(), lembrete.getTitulo(), status);
                lembreteText.setText(textoLembrete);

                if (isEditMode) {
                    deleteItemIcon.setVisibility(View.VISIBLE);
                    deleteItemIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.red_dark), PorterDuff.Mode.SRC_IN);
                    deleteItemIcon.setOnClickListener(v -> showDeleteConfirmationDialog(lembrete));

                    lembreteText.setOnClickListener(v -> showLembreteDialog(lembrete));

                } else {
                    deleteItemIcon.setVisibility(View.GONE);
                    lembreteText.setOnClickListener(null); // Desativa clique para edição
                }
                containerLembretesDoDia.addView(lembreteItemView);
            }

            dataHeader.setOnClickListener(v -> {
                containerLembretesDoDia.setVisibility(
                        containerLembretesDoDia.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
                );
            });
            binding.containerLembretes.addView(groupView);
        }
    }

    private void showDeleteConfirmationDialog(final Lembrete lembrete) {
        if (lembrete == null || lembrete.getId() == null) {
            Log.e("DELETE", "Lembrete ou ID nulo!");
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir Lembrete")
                .setMessage("Tem certeza que deseja excluir este lembrete?")
                .setPositiveButton("Excluir", (dialog, which) -> deletarLembrete(lembrete.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void atualizarPainelDeResumo() {
        // Agora usamos a lista de backend (listaDeLembretes)
        long hojeCount = listaDeLembretes.stream().filter(l -> isToday(l.getData())).count();
        long programadosCount = listaDeLembretes.stream().filter(l -> !l.isAtivo()).count();
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
            Date date = parseDateFormat.parse(dataStr);
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


    private void agendarLembrete(int hora, int minuto, String titulo, String mensagem) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hora);
        c.set(Calendar.MINUTE, minuto);
        c.set(Calendar.SECOND, 0);

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(getContext(), LembreteReceiver.class);
        intent.putExtra("titulo", titulo);
        intent.putExtra("mensagem", mensagem);

        // Usamos um ID baseado no tempo para ter múltiplos alarmes
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            c.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            c.getTimeInMillis(),
                            pendingIntent
                    );
                    Toast.makeText(getContext(), "Permissão de alarmes exatos necessária. Agendado como aproximado.", Toast.LENGTH_LONG).show();
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        c.getTimeInMillis(),
                        pendingIntent
                );
            }


        } else {
            Toast.makeText(getContext(), "Não foi possível acessar AlarmManager.", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}