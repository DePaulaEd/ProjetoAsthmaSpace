package br.fmu.projetoasthmaspace;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import br.fmu.projetoasthmaspace.databinding.ActivityLembretesBinding;
import br.fmu.projetoasthmaspace.databinding.CardLembreteStatBinding;

public class Lembretes extends Fragment {

    private ActivityLembretesBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityLembretesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.fabNovoLembrete.setOnClickListener(v -> showNovoLembreteDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        atualizarPainelDeResumo();
        atualizarListaDeLembretes();
    }

    private void atualizarPainelDeResumo() {
        long hojeCount = MainActivity.listaDeLembretes.stream().filter(l -> isToday(l.data)).count();
        long programadosCount = MainActivity.listaDeLembretes.stream().filter(l -> isToday(l.data) && !l.concluida).count();
        long todosCount = MainActivity.listaDeLembretes.size();
        long concluidosCount = MainActivity.listaDeLembretes.stream().filter(l -> l.concluida).count();

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

    private boolean isToday(Date date) {
        Calendar hoje = Calendar.getInstance();
        Calendar dataLembrete = Calendar.getInstance();
        dataLembrete.setTime(date);
        return hoje.get(Calendar.YEAR) == dataLembrete.get(Calendar.YEAR) &&
               hoje.get(Calendar.DAY_OF_YEAR) == dataLembrete.get(Calendar.DAY_OF_YEAR);
    }

    private void showNovoLembreteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_novo_lembrete, null);
        final EditText tituloInput = dialogView.findViewById(R.id.edit_text_titulo_lembrete);
        final EditText horarioInput = dialogView.findViewById(R.id.edit_text_horario_lembrete);

        builder.setView(dialogView)
                .setPositiveButton("Salvar", (dialog, id) -> {
                    String titulo = tituloInput.getText().toString();
                    String horario = horarioInput.getText().toString();
                    if (titulo.isEmpty() || horario.isEmpty()) {
                        Toast.makeText(getContext(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    MainActivity.listaDeLembretes.add(new MainActivity.Lembrete(titulo, horario, new Date()));
                    Toast.makeText(getContext(), "Lembrete salvo!", Toast.LENGTH_LONG).show();
                    atualizarPainelDeResumo();
                    atualizarListaDeLembretes();
                })
                .setNegativeButton("Cancelar", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void atualizarListaDeLembretes() {
        if (binding == null) return;
        binding.containerLembretes.removeAllViews();

        Map<String, List<MainActivity.Lembrete>> lembretesAgrupados = MainActivity.listaDeLembretes.stream()
                .sorted(Comparator.comparing((MainActivity.Lembrete lembrete) -> lembrete.data).reversed())
                .collect(Collectors.groupingBy(lembrete -> new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault()).format(lembrete.data)));

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (Map.Entry<String, List<MainActivity.Lembrete>> entry : lembretesAgrupados.entrySet()) {
            View groupView = inflater.inflate(R.layout.item_data_expandable, binding.containerLembretes, false);
            
            TextView dataHeader = groupView.findViewById(R.id.text_data_header);
            LinearLayout containerLembretesDoDia = groupView.findViewById(R.id.container_lembretes_do_dia);

            dataHeader.setText(entry.getKey());

            List<MainActivity.Lembrete> lembretesDoDia = entry.getValue();
            lembretesDoDia.sort(Comparator.comparing(lembrete -> lembrete.horario));

            for (MainActivity.Lembrete lembrete : lembretesDoDia) {
                TextView lembreteView = new TextView(getContext());
                String status = lembrete.concluida ? "(Concluído)" : "(Programado)";
                String textoLembrete = String.format(Locale.getDefault(), "%s - %s %s", lembrete.horario, lembrete.titulo, status);
                lembreteView.setText(textoLembrete);
                lembreteView.setTextColor(getResources().getColor(R.color.white));
                lembreteView.setTextSize(16);
                lembreteView.setPadding(16, 8, 16, 8);
                containerLembretesDoDia.addView(lembreteView);
            }
            
            dataHeader.setOnClickListener(v -> {
                if (containerLembretesDoDia.getVisibility() == View.GONE) {
                    containerLembretesDoDia.setVisibility(View.VISIBLE);
                } else {
                    containerLembretesDoDia.setVisibility(View.GONE);
                }
            });

            binding.containerLembretes.addView(groupView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
