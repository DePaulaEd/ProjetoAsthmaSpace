package br.fmu.projetoasthmaspace;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
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

import br.fmu.projetoasthmaspace.databinding.ActivityLembretesBinding;
import br.fmu.projetoasthmaspace.databinding.CardLembreteStatBinding;

public class Lembretes extends Fragment {

    private ActivityLembretesBinding binding;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityLembretesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.fabNovoLembrete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLembreteDialog(null);
            }
        });

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
        atualizarPainelDeResumo();
        atualizarListaDeLembretes();
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

    private void showLembreteDialog(@Nullable final MainActivity.Lembrete lembreteExistente) {
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
            tituloInput.setText(lembreteExistente.titulo);
            horarioInput.setText(lembreteExistente.horario);
        } else {
            dialogTitle.setText("Novo Lembrete");
        }
        builder.setCustomTitle(dialogTitle);

        builder.setView(dialogView);
        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String titulo = tituloInput.getText().toString();
                String horario = horarioInput.getText().toString();
                if (titulo.trim().isEmpty() || horario.trim().isEmpty()) {
                    Toast.makeText(getContext(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isEditing) {
                    lembreteExistente.titulo = titulo;
                    lembreteExistente.horario = horario;
                    Toast.makeText(getContext(), "Lembrete atualizado!", Toast.LENGTH_SHORT).show();
                } else {
                    MainActivity.listaDeLembretes.add(new MainActivity.Lembrete(titulo, horario, new Date()));
                    Toast.makeText(getContext(), "Lembrete salvo!", Toast.LENGTH_SHORT).show();
                }
                atualizarPainelDeResumo();
                atualizarListaDeLembretes();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void atualizarListaDeLembretes() {
        if (binding == null) return;
        binding.containerLembretes.removeAllViews();

        // Agrupamento manual dos lembretes por data
        Map<String, List<MainActivity.Lembrete>> lembretesAgrupados = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault());
        for (MainActivity.Lembrete lembrete : MainActivity.listaDeLembretes) {
            String dataFormatada = sdf.format(lembrete.data);
            if (!lembretesAgrupados.containsKey(dataFormatada)) {
                lembretesAgrupados.put(dataFormatada, new ArrayList<MainActivity.Lembrete>());
            }
            lembretesAgrupados.get(dataFormatada).add(lembrete);
        }

        // Ordenar as datas em ordem decrescente (mais recentes primeiro)
        List<String> datasOrdenadas = new ArrayList<>(lembretesAgrupados.keySet());
        Collections.sort(datasOrdenadas, new Comparator<String>() {
             final SimpleDateFormat format = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault());
             @Override
             public int compare(String s1, String s2) {
                 try {
                     Date d1 = format.parse(s1);
                     Date d2 = format.parse(s2);
                     return d2.compareTo(d1); // Ordem decrescente
                 } catch (ParseException e) {
                     return 0;
                 }
             }
        });

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (String dataKey : datasOrdenadas) {
            final List<MainActivity.Lembrete> lembretesDoDia = lembretesAgrupados.get(dataKey);

            View groupView = inflater.inflate(R.layout.item_data_expandable, binding.containerLembretes, false);
            TextView dataHeader = groupView.findViewById(R.id.text_data_header);
            final ImageView deleteGroupIcon = groupView.findViewById(R.id.icon_delete_group);
            final LinearLayout containerLembretesDoDia = groupView.findViewById(R.id.container_lembretes_do_dia);

            dataHeader.setText(dataKey);

            Collections.sort(lembretesDoDia, new Comparator<MainActivity.Lembrete>() {
                @Override
                public int compare(MainActivity.Lembrete o1, MainActivity.Lembrete o2) {
                    return o1.horario.compareTo(o2.horario);
                }
            });

            if (isEditMode) {
                deleteGroupIcon.setVisibility(View.VISIBLE);
                deleteGroupIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.red_dark), PorterDuff.Mode.SRC_IN);
                deleteGroupIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeleteConfirmationDialog(null, lembretesDoDia);
                    }
                });
            } else {
                deleteGroupIcon.setVisibility(View.GONE);
            }

            containerLembretesDoDia.removeAllViews();
            for (final MainActivity.Lembrete lembrete : lembretesDoDia) {
                View lembreteItemView = inflater.inflate(R.layout.item_lembrete_edit, containerLembretesDoDia, false);
                TextView lembreteText = lembreteItemView.findViewById(R.id.text_lembrete);
                ImageView deleteItemIcon = lembreteItemView.findViewById(R.id.icon_delete_item);

                String status = lembrete.concluida ? "(Concluído)" : "(Programado)";
                String textoLembrete = String.format(Locale.getDefault(), "%s - %s %s", lembrete.horario, lembrete.titulo, status);
                lembreteText.setText(textoLembrete);

                if (isEditMode) {
                    deleteItemIcon.setVisibility(View.VISIBLE);
                    deleteItemIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.red_dark), PorterDuff.Mode.SRC_IN);
                    deleteItemIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDeleteConfirmationDialog(lembrete, null);
                        }
                    });
                    lembreteText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showLembreteDialog(lembrete);
                        }
                    });
                } else {
                    deleteItemIcon.setVisibility(View.GONE);
                    lembreteText.setOnClickListener(null);
                }
                containerLembretesDoDia.addView(lembreteItemView);
            }

            dataHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (containerLembretesDoDia.getVisibility() == View.GONE) {
                        containerLembretesDoDia.setVisibility(View.VISIBLE);
                    } else {
                        containerLembretesDoDia.setVisibility(View.GONE);
                    }
                }
            });
            binding.containerLembretes.addView(groupView);
        }
    }

    private void showDeleteConfirmationDialog(@Nullable final MainActivity.Lembrete lembrete, @Nullable final List<MainActivity.Lembrete> grupo) {
        new AlertDialog.Builder(getContext())
                .setTitle("Excluir")
                .setMessage("Tem certeza que deseja excluir? Esta ação não pode ser desfeita.")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (lembrete != null) {
                            MainActivity.listaDeLembretes.remove(lembrete);
                        } else if (grupo != null) {
                            MainActivity.listaDeLembretes.removeAll(grupo);
                        }
                        atualizarPainelDeResumo();
                        atualizarListaDeLembretes();
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void atualizarPainelDeResumo() {
        long hojeCount = 0;
        long programadosCount = 0;
        long concluidosCount = 0;
        for (MainActivity.Lembrete l : MainActivity.listaDeLembretes) {
            if (isToday(l.data)) {
                hojeCount++;
            }
            if (!l.concluida) {
                programadosCount++;
            }
            if (l.concluida) {
                concluidosCount++;
            }
        }
        long todosCount = MainActivity.listaDeLembretes.size();

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
        if (date == null) return false;
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
