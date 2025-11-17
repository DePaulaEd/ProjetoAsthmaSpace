package br.fmu.projetoasthmaspace;

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
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import br.fmu.projetoasthmaspace.databinding.ActivityDiarioSintomasBinding;

public class DiarioSintomas extends Fragment {

    private ActivityDiarioSintomasBinding binding;
    private List<Anotacao> listaDeAnotacoes = new ArrayList<>();

    // Modelo de dados para uma Anotação, agora com a data completa
    private static class Anotacao {
        String titulo;
        String descricao;
        Date dataHora;

        Anotacao(String titulo, String descricao, Date dataHora) {
            this.titulo = titulo;
            this.descricao = descricao;
            this.dataHora = dataHora;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityDiarioSintomasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        preencherComExemplos(); // Adiciona os dados de exemplo

        binding.fabAdicionarSintoma.setOnClickListener(v -> showNovoSintomaDialog());

        atualizarTela(); // Atualiza toda a interface
    }

    private void preencherComExemplos() {
        Calendar cal = Calendar.getInstance();
        
        // Hoje
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 15);
        listaDeAnotacoes.add(new Anotacao("Falta de ar leve", "Senti um pouco de falta de ar ao acordar.", cal.getTime()));

        // Ontem
        cal.add(Calendar.DAY_OF_YEAR, -1);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        cal.set(Calendar.MINUTE, 5);
        listaDeAnotacoes.add(new Anotacao("Tosse noturna", "Tive uma crise de tosse antes de dormir.", cal.getTime()));
        
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 30);
        listaDeAnotacoes.add(new Anotacao("Chiado no peito", "Chiado leve durante a tarde, usei a bombinha.", cal.getTime()));

        // Anteontem
        cal.add(Calendar.DAY_OF_YEAR, -1);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        listaDeAnotacoes.add(new Anotacao("Nenhum sintoma", "Acordei bem, sem sintomas aparentes.", cal.getTime()));
    }
    
    private void atualizarTela() {
        // Define a data atual no cabeçalho "Hoje"
        SimpleDateFormat formatadorDataHoje = new SimpleDateFormat("dd 'de' MMMM", Locale.getDefault());
        binding.textDataAtual.setText(formatadorDataHoje.format(new Date()));

        // Separa as anotações
        List<Anotacao> anotacoesDeHoje;
        Map<String, List<Anotacao>> anotacoesAnterioresAgrupadas;

        SimpleDateFormat formatadorDataChave = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        anotacoesDeHoje = listaDeAnotacoes.stream()
                .filter(a -> isToday(a.dataHora))
                .collect(Collectors.toList());

        anotacoesAnterioresAgrupadas = listaDeAnotacoes.stream()
                .filter(a -> !isToday(a.dataHora))
                .collect(Collectors.groupingBy(a -> formatadorDataChave.format(a.dataHora)));
                
        // Exibe as anotações
        exibirAnotacoes(binding.containerAnotacoesHoje, anotacoesDeHoje);
        exibirDatasAnteriores(anotacoesAnterioresAgrupadas);
    }
    
    private boolean isToday(Date date) {
        Calendar hoje = Calendar.getInstance();
        Calendar dataAnotacao = Calendar.getInstance();
        dataAnotacao.setTime(date);
        return hoje.get(Calendar.YEAR) == dataAnotacao.get(Calendar.YEAR) &&
               hoje.get(Calendar.DAY_OF_YEAR) == dataAnotacao.get(Calendar.DAY_OF_YEAR);
    }

    private void exibirAnotacoes(LinearLayout container, List<Anotacao> anotacoes) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        SimpleDateFormat formatadorHorario = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (Anotacao anotacao : anotacoes) {
            View itemView = inflater.inflate(R.layout.item_anotacao_diario, container, false);

            TextView itemHorario = itemView.findViewById(R.id.item_horario);
            TextView itemTitulo = itemView.findViewById(R.id.item_titulo);
            TextView itemDescricao = itemView.findViewById(R.id.item_descricao);

            itemHorario.setText(formatadorHorario.format(anotacao.dataHora));
            itemTitulo.setText(anotacao.titulo);
            itemDescricao.setText(anotacao.descricao);

            container.addView(itemView);
        }
    }

    private void exibirDatasAnteriores(Map<String, List<Anotacao>> anotacoesAgrupadas) {
        binding.containerAnotacoesAnteriores.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        SimpleDateFormat formatadorDataDisplay = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault());

        for (Map.Entry<String, List<Anotacao>> entry : anotacoesAgrupadas.entrySet()) {
            List<Anotacao> anotacoesDoDia = entry.getValue();
            if (anotacoesDoDia.isEmpty()) continue;

            View itemView = inflater.inflate(R.layout.item_data_anterior, binding.containerAnotacoesAnteriores, false);
            TextView dataAnteriorText = itemView.findViewById(R.id.text_data_anterior_item);
            
            Date dataDoGrupo = anotacoesDoDia.get(0).dataHora;
            dataAnteriorText.setText(formatadorDataDisplay.format(dataDoGrupo));
            
            itemView.setOnClickListener(v -> showDialogAnteriores(dataDoGrupo, anotacoesDoDia));

            binding.containerAnotacoesAnteriores.addView(itemView);
        }
    }

    private void showDialogAnteriores(Date data, List<Anotacao> anotacoes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_anotacoes_anteriores, null);

        TextView tituloDialog = dialogView.findViewById(R.id.dialog_data_anterior_titulo);
        Button btnFechar = dialogView.findViewById(R.id.dialog_btn_fechar_anterior);
        LinearLayout containerDialog = dialogView.findViewById(R.id.dialog_container_anotacoes_anteriores);
        
        SimpleDateFormat formatadorDataDisplay = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault());
        tituloDialog.setText("Anotações de " + formatadorDataDisplay.format(data));
        
        exibirAnotacoes(containerDialog, anotacoes);

        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        
        btnFechar.setOnClickListener(v -> alertDialog.dismiss());
        
        alertDialog.show();
    }

    private void showNovoSintomaDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_novo_sintoma, null);

        final EditText tituloInput = dialogView.findViewById(R.id.edit_text_titulo_sintoma);
        final EditText descricaoInput = dialogView.findViewById(R.id.edit_text_descricao_sintoma);

        builder.setView(dialogView)
                .setPositiveButton("Salvar", (dialog, id) -> {
                    String titulo = tituloInput.getText().toString();
                    String descricao = descricaoInput.getText().toString();

                    if (titulo.isEmpty() || descricao.isEmpty()) {
                        Toast.makeText(getContext(), "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Adicionar nova anotação à lista com a data/hora atual
                    listaDeAnotacoes.add(new Anotacao(titulo, descricao, new Date()));

                    // Atualizar toda a interface
                    atualizarTela();

                    Toast.makeText(getContext(), "Anotação salva com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, id) -> {
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
