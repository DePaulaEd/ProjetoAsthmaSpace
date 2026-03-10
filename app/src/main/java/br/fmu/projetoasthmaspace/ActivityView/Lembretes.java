package br.fmu.projetoasthmaspace.ActivityView;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
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
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));

    // =============================================
    // FILTRO ATIVO: null = todos, "hoje", "programados", "concluidos"
    // =============================================
    private String filtroAtivo = null;

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

        binding.fabNovoLembrete.setOnClickListener(v -> showLembreteDialog(null));

        binding.fabEditarLembrete.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            toggleUiForEditMode();
            atualizarListaDeLembretes();
        });

        // =============================================
        // FILTROS: clique nos cards de resumo
        // =============================================
        binding.statHoje.getRoot().setOnClickListener(v -> toggleFiltro("hoje"));
        binding.statProgramados.getRoot().setOnClickListener(v -> toggleFiltro("programados"));
        binding.statConcluidos.getRoot().setOnClickListener(v -> toggleFiltro("concluidos"));
        binding.statTodos.getRoot().setOnClickListener(v -> toggleFiltro(null));
    }

    // Alterna filtro: se clicar no mesmo, desativa
    private void toggleFiltro(String filtro) {
        if (filtro == null || filtro.equals(filtroAtivo)) {
            filtroAtivo = null;
        } else {
            filtroAtivo = filtro;
        }
        destacarCardFiltro();
        atualizarListaDeLembretes();
    }

    // Destaca visualmente o card de filtro ativo
    private void destacarCardFiltro() {
        float alphaAtivo = 1.0f;
        float alphaInativo = 0.5f;

        binding.statHoje.getRoot().setAlpha("hoje".equals(filtroAtivo) ? alphaAtivo : (filtroAtivo == null ? alphaAtivo : alphaInativo));
        binding.statProgramados.getRoot().setAlpha("programados".equals(filtroAtivo) ? alphaAtivo : (filtroAtivo == null ? alphaAtivo : alphaInativo));
        binding.statConcluidos.getRoot().setAlpha("concluidos".equals(filtroAtivo) ? alphaAtivo : (filtroAtivo == null ? alphaAtivo : alphaInativo));
        binding.statTodos.getRoot().setAlpha(filtroAtivo == null ? alphaAtivo : alphaInativo);
    }

    // Retorna lista filtrada conforme filtroAtivo
    private List<Lembrete> getListaFiltrada() {
        if (filtroAtivo == null) return new ArrayList<>(listaDeLembretes);

        List<Lembrete> filtrada = new ArrayList<>();
        for (Lembrete l : listaDeLembretes) {
            switch (filtroAtivo) {
                case "hoje":
                    if (isToday(l.getData())) filtrada.add(l);
                    break;
                case "programados":
                    if (!l.isAtivo()) filtrada.add(l);
                    break;
                case "concluidos":
                    if (l.isAtivo()) filtrada.add(l);
                    break;
            }
        }
        return filtrada;
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

    // --- Backend ---

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
                                parseDateFormat.parse(dataStr); // valida
                                tempLista.add(new Lembrete(
                                        resp.getId(),
                                        resp.getTitulo(),
                                        "",
                                        dataStr,
                                        resp.getHorario(),
                                        resp.isConcluido()
                                ));
                            } catch (ParseException e) {
                                Log.e("Lembretes", "Erro ao parsear data id: " + resp.getId(), e);
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

    private void atualizarLembrete(Lembrete lembrete, String novoTitulo, String novoHorario, String novaData) {
        api = ApiClient.getApiService(requireContext());

        LembreteUpdateRequest req = new LembreteUpdateRequest(
                novoTitulo,
                novaData,  // ← agora usa a data selecionada no DatePicker
                novoHorario,
                lembrete.isAtivo()
        );

        api.atualizarDados(lembrete.getId(), req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Lembrete atualizado!", Toast.LENGTH_SHORT).show();
                    carregarLembretesDoBackend();
                } else {
                    Toast.makeText(getContext(), "Erro ao atualizar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Falha de conexão!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletarLembrete(Long id) {
        Context ctx = getContext();
        if (ctx == null) return;

        api = ApiClient.getApiService(requireContext());

        api.deletarLembrete(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Lembrete excluído!", Toast.LENGTH_SHORT).show();
                    carregarLembretesDoBackend();
                } else {
                    Toast.makeText(getContext(), "Erro ao excluir (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Falha de conexão!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =============================================
    // DIALOG MELHORADO com DatePicker
    // =============================================
    private void showLembreteDialog(@Nullable final Lembrete lembreteExistente) {
        final boolean isEditing = lembreteExistente != null;

        // Data selecionada — começa com hoje ou com a data do lembrete
        final String[] dataSelecionada = {
                isEditing ? lembreteExistente.getData() : parseDateFormat.format(new Date())
        };

        // Infla o layout do dialog
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_novo_lembrete, null);
        final EditText tituloInput = dialogView.findViewById(R.id.edit_text_titulo_lembrete);
        final EditText horarioInput = dialogView.findViewById(R.id.edit_text_horario_lembrete);

        // Botão para selecionar data (você pode adicionar esse TextView no dialog_novo_lembrete.xml)
        // Se não tiver o campo, vamos usar um TextView criado dinamicamente
        TextView btnSelecionarData = new TextView(getContext());
        btnSelecionarData.setPadding(0, 8, 0, 8);
        btnSelecionarData.setTextColor(Color.parseColor("#4FC3F7"));
        btnSelecionarData.setTextSize(14f);
        btnSelecionarData.setText("📅 Data: " + formatarDataParaExibicao(dataSelecionada[0]));
        btnSelecionarData.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            try {
                Date d = parseDateFormat.parse(dataSelecionada[0]);
                cal.setTime(d);
            } catch (ParseException ignored) {}

            new DatePickerDialog(
                    requireContext(),
                    (picker, year, month, dayOfMonth) -> {
                        Calendar selecionado = Calendar.getInstance();
                        selecionado.set(year, month, dayOfMonth);
                        dataSelecionada[0] = parseDateFormat.format(selecionado.getTime());
                        btnSelecionarData.setText("📅 Data: " + formatarDataParaExibicao(dataSelecionada[0]));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Adiciona o botão de data no layout do dialog
        if (dialogView instanceof ViewGroup) {
            ((ViewGroup) dialogView).addView(btnSelecionarData, 0);
        }

        if (isEditing) {
            tituloInput.setText(lembreteExistente.getTitulo());
            horarioInput.setText(lembreteExistente.getHorario());
        }

        // Cria o dialog com visual customizado
        AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.DialogTheme)
                .setTitle(isEditing ? "Editar Lembrete" : "Novo Lembrete")
                .setView(dialogView)
                .setPositiveButton("Salvar", null) // null para controlar o dismiss manualmente
                .setNegativeButton("Cancelar", (d, id) -> d.dismiss())
                .create();

        // Estiliza o título
        dialog.setOnShowListener(d -> {
            // Cor do botão positivo
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#4FC3F7"));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#8EADD4"));

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String titulo = tituloInput.getText().toString().trim();
                String horario = horarioInput.getText().toString().trim();

                if (titulo.isEmpty() || horario.isEmpty()) {
                    Toast.makeText(getContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    String[] partes = horario.split(":");
                    int hora = Integer.parseInt(partes[0]);
                    int minuto = Integer.parseInt(partes[1]);

                    if (isEditing) {
                        atualizarLembrete(lembreteExistente, titulo, horario, dataSelecionada[0]);
                    } else {
                        LembreteRequest req = new LembreteRequest(titulo, dataSelecionada[0], horario);
                        registrarLembrete(req, hora, minuto, titulo);
                    }
                    dialog.dismiss();

                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    Toast.makeText(getContext(), "Formato inválido. Use HH:mm", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private String formatarDataParaExibicao(String dataStr) {
        try {
            Date d = parseDateFormat.parse(dataStr);
            return displayDateFormat.format(d);
        } catch (ParseException e) {
            return dataStr;
        }
    }

    // =============================================
    // LISTA — usa getListaFiltrada()
    // =============================================
    private void atualizarListaDeLembretes() {
        if (binding == null) return;
        binding.containerLembretes.removeAllViews();

        List<Lembrete> listaParaExibir = getListaFiltrada();

        if (listaParaExibir.isEmpty()) {
            TextView vazio = new TextView(getContext());
            vazio.setText(filtroAtivo == null ? "Nenhum lembrete cadastrado." : "Nenhum lembrete nesta categoria.");
            vazio.setTextColor(Color.parseColor("#8EADD4"));
            vazio.setTextSize(14f);
            vazio.setPadding(0, 24, 0, 0);
            binding.containerLembretes.addView(vazio);
            return;
        }

        Map<String, List<Lembrete>> lembretesAgrupados = new HashMap<>();
        for (Lembrete lembrete : listaParaExibir) {
            try {
                Date date = parseDateFormat.parse(lembrete.getData());
                String dataFormatada = displayDateFormat.format(date);
                if (!lembretesAgrupados.containsKey(dataFormatada)) {
                    lembretesAgrupados.put(dataFormatada, new ArrayList<>());
                }
                lembretesAgrupados.get(dataFormatada).add(lembrete);
            } catch (ParseException e) {
                Log.e("Lembretes", "Erro ao parsear data: " + lembrete.getData(), e);
            }
        }

        List<String> datasOrdenadas = new ArrayList<>(lembretesAgrupados.keySet());
        Collections.sort(datasOrdenadas, (s1, s2) -> {
            try {
                Date d1 = displayDateFormat.parse(s1);
                Date d2 = displayDateFormat.parse(s2);
                return d2.compareTo(d1);
            } catch (ParseException e) {
                return 0;
            }
        });

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (String dataKey : datasOrdenadas) {
            final List<Lembrete> lembretesDoDia = lembretesAgrupados.get(dataKey);

            View groupView = inflater.inflate(R.layout.item_data_expandable, binding.containerLembretes, false);
            TextView dataHeader = groupView.findViewById(R.id.text_data_header);
            ImageView deleteGroupIcon = groupView.findViewById(R.id.icon_delete_group);
            final LinearLayout containerLembretesDoDia = groupView.findViewById(R.id.container_lembretes_do_dia);

            dataHeader.setText(dataKey);
            deleteGroupIcon.setVisibility(View.GONE);
            lembretesDoDia.sort(Comparator.comparing(Lembrete::getHorario));

            containerLembretesDoDia.removeAllViews();
            for (final Lembrete lembrete : lembretesDoDia) {
                View lembreteItemView = inflater.inflate(R.layout.item_lembrete_edit, containerLembretesDoDia, false);
                TextView lembreteText = lembreteItemView.findViewById(R.id.text_lembrete);
                ImageView deleteItemIcon = lembreteItemView.findViewById(R.id.icon_delete_item);

                String status = lembrete.isAtivo() ? "(Concluído)" : "(Programado)";
                lembreteText.setText(String.format(Locale.getDefault(), "%s - %s %s",
                        lembrete.getHorario(), lembrete.getTitulo(), status));

                if (isEditMode) {
                    deleteItemIcon.setVisibility(View.VISIBLE);
                    deleteItemIcon.setColorFilter(
                            ContextCompat.getColor(getContext(), R.color.red_dark),
                            PorterDuff.Mode.SRC_IN);
                    deleteItemIcon.setOnClickListener(v -> showDeleteConfirmationDialog(lembrete));
                    lembreteText.setOnClickListener(v -> showLembreteDialog(lembrete));
                } else {
                    deleteItemIcon.setVisibility(View.GONE);
                    lembreteText.setOnClickListener(null);
                }
                containerLembretesDoDia.addView(lembreteItemView);
            }

            dataHeader.setOnClickListener(v ->
                    containerLembretesDoDia.setVisibility(
                            containerLembretesDoDia.getVisibility() == View.GONE
                                    ? View.VISIBLE : View.GONE));

            binding.containerLembretes.addView(groupView);
        }
    }

    private void showDeleteConfirmationDialog(final Lembrete lembrete) {
        if (lembrete == null || lembrete.getId() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir Lembrete")
                .setMessage("Tem certeza que deseja excluir este lembrete?")
                .setPositiveButton("Excluir", (dialog, which) -> deletarLembrete(lembrete.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void atualizarPainelDeResumo() {
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
            return false;
        }
    }

    private void agendarLembrete(int hora, int minuto, String titulo, String mensagem) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hora);
        c.set(Calendar.MINUTE, minuto);
        c.set(Calendar.SECOND, 0);
        if (c.before(Calendar.getInstance())) c.add(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(getContext(), LembreteReceiver.class);
        intent.putExtra("titulo", titulo);
        intent.putExtra("mensagem", mensagem);

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
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                    Toast.makeText(getContext(), "Agendado como alarme aproximado.", Toast.LENGTH_LONG).show();
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}