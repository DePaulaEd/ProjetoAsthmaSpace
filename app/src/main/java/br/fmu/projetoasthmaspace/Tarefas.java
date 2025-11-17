package br.fmu.projetoasthmaspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import br.fmu.projetoasthmaspace.databinding.ActivityTarefasBinding;

public class Tarefas extends Fragment {

    private ActivityTarefasBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityTarefasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        atualizarListaDeTarefas();
    }

    private void atualizarListaDeTarefas() {
        if (binding == null) return;

        binding.containerTarefas.removeAllViews();

        List<MainActivity.Lembrete> tarefasDeHoje = MainActivity.listaDeLembretes.stream()
                .filter(lembrete -> !lembrete.concluida && isToday(lembrete.data))
                .sorted(Comparator.comparing(lembrete -> lembrete.horario)) // Ordena por horário
                .collect(Collectors.toList());

        // Atualiza o contador
        int numeroDeTarefas = tarefasDeHoje.size();
        if (numeroDeTarefas == 1) {
            binding.contadorTarefas.setText("1 tarefa pendente");
        } else {
            binding.contadorTarefas.setText(String.format(Locale.getDefault(), "%d tarefas pendentes", numeroDeTarefas));
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (MainActivity.Lembrete tarefa : tarefasDeHoje) {
            View itemView = inflater.inflate(R.layout.item_tarefa_checkbox, binding.containerTarefas, false);
            CheckBox checkBoxTarefa = itemView.findViewById(R.id.checkbox_tarefa);

            String textoTarefa = tarefa.titulo + " - " + tarefa.horario;
            checkBoxTarefa.setText(textoTarefa);

            checkBoxTarefa.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Concluir Tarefa")
                            .setMessage("Deseja marcar esta tarefa como concluída?")
                            .setPositiveButton("Sim", (dialog, which) -> {
                                tarefa.concluida = true;
                                atualizarListaDeTarefas();
                            })
                            .setNegativeButton("Não", (dialog, which) -> {
                                buttonView.setChecked(false);
                            })
                            .show();
                }
            });

            binding.containerTarefas.addView(itemView);
        }
    }

    private boolean isToday(Date date) {
        Calendar hoje = Calendar.getInstance();
        Calendar dataLembrete = Calendar.getInstance();
        dataLembrete.setTime(date);
        return hoje.get(Calendar.YEAR) == dataLembrete.get(Calendar.YEAR) &&
                hoje.get(Calendar.DAY_OF_YEAR) == dataLembrete.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
