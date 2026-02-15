package br.fmu.projetoasthmaspace.ActivityView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import br.fmu.projetoasthmaspace.Domain.DiarioParser;
import br.fmu.projetoasthmaspace.Domain.DiarioRequest;
import br.fmu.projetoasthmaspace.Domain.DiarioResponse;
import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.Service.ApiClient;
import br.fmu.projetoasthmaspace.Service.ApiService;
import br.fmu.projetoasthmaspace.databinding.ActivityDiarioSintomasBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiarioSintomas extends Fragment {

    private ActivityDiarioSintomasBinding binding;
    private List<DiarioResponse> diario;
    private List<DiarioResponse> hoje;
    private String token;
    private ApiService api;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityDiarioSintomasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("APP", Context.MODE_PRIVATE);
        token = prefs.getString("TOKEN", null);
        api = ApiClient.getApiService(requireContext());


        binding.fabAdicionarSintoma.setOnClickListener(v -> showNovoSintomaDialog());
        binding.fabEditarSintoma.setOnClickListener(v -> toggleEditMode());

        carregarDiario();
    }

    private void toggleEditMode() {
        if (isEditMode) {
            salvarAlteracoes();
        } else {
            isEditMode = true;
            binding.fabEditarSintoma.setImageResource(R.drawable.ic_save);
            binding.fabAdicionarSintoma.setVisibility(View.GONE);
            exibirAnotacoes(binding.containerAnotacoesHoje, hoje, true);
        }
    }

    private void carregarDiario() {
        api.listarDiario().enqueue(new Callback<List<DiarioResponse>>() {
            @Override
            public void onResponse(Call<List<DiarioResponse>> call, Response<List<DiarioResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    diario = response.body();
                    atualizarTela();
                } else {
                    Toast.makeText(getContext(), "Erro ao carregar diário", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DiarioResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarTela() {
        if (diario == null || binding == null) return;

        SimpleDateFormat dfHoje = new SimpleDateFormat("dd 'de' MMMM", Locale.getDefault());
        dfHoje.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

        Date agora = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo")).getTime();
        binding.textDataAtual.setText(dfHoje.format(agora));


        hoje = new ArrayList<>();
        Map<String, List<DiarioResponse>> anteriores = new HashMap<>();
        for (DiarioResponse d : diario) {
            if (DiarioParser.isToday(d.getData())) {
                hoje.add(d);
            } else {
                if (!anteriores.containsKey(d.getData())) {
                    anteriores.put(d.getData(), new ArrayList<>());
                }
                anteriores.get(d.getData()).add(d);
            }
        }

        exibirAnotacoes(binding.containerAnotacoesHoje, hoje, isEditMode);
        exibirDatasAnteriores(anteriores);
    }

    private void exibirAnotacoes(LinearLayout container, List<DiarioResponse> lista, boolean editMode) {
        container.removeAllViews();
        if (getContext() == null || lista == null) return;
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (final DiarioResponse resp : lista) {
            View itemView;
            if (editMode) {
                itemView = inflater.inflate(R.layout.item_anotacao_diario_edit, container, false);
                EditText editTitulo = itemView.findViewById(R.id.edit_titulo_sintoma);
                EditText editDescricao = itemView.findViewById(R.id.edit_descricao_sintoma);
                editTitulo.setText(resp.getIntensidade());
                editDescricao.setText(resp.getDescricao());
                itemView.findViewById(R.id.btn_excluir_anotacao).setOnClickListener(v -> excluirAnotacao(resp.getId()));
            } else {
                itemView = inflater.inflate(R.layout.item_anotacao_diario, container, false);
                TextView itemHorario = itemView.findViewById(R.id.item_horario);
                TextView itemTitulo = itemView.findViewById(R.id.item_titulo);
                TextView itemDescricao = itemView.findViewById(R.id.item_descricao);

                SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date hora = DiarioParser.parseHorario(resp.getHorario());
                itemHorario.setText(hora != null ? formatoHora.format(hora) : resp.getHorario());
                itemTitulo.setText("Causa: " + resp.getIntensidade());
                itemDescricao.setText(resp.getDescricao() != null ? resp.getDescricao() : "");

                itemView.setOnClickListener(v -> {
                    String horarioFormatado = (hora != null) ? formatoHora.format(hora) : resp.getHorario();
                    String tituloDialogo = "Intensidade: " + resp.getIntensidade();
                    String descricaoFormatada = (resp.getDescricao() != null && !resp.getDescricao().isEmpty()) ? resp.getDescricao() : "(Nenhuma descrição fornecida)";
                    String mensagemDialogo = "Anotado às: " + horarioFormatado + "\n\n" + descricaoFormatada;

                    new AlertDialog.Builder(getContext())
                            .setTitle(tituloDialogo)
                            .setMessage(mensagemDialogo)
                            .setPositiveButton("Fechar", null)
                            .show();
                });
            }
            itemView.setTag(resp);
            container.addView(itemView);
        }
    }

    private void exibirDatasAnteriores(Map<String, List<DiarioResponse>> map) {
        if (binding == null) return;
        binding.containerAnotacoesAnteriores.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        SimpleDateFormat dfDisplay = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault());

        for (Map.Entry<String, List<DiarioResponse>> entry : map.entrySet()) {
            final String dataString = entry.getKey();
            final List<DiarioResponse> lista = entry.getValue();
            Date dia = DiarioParser.parseData(dataString);

            View item = inflater.inflate(R.layout.item_data_anterior, binding.containerAnotacoesAnteriores, false);
            TextView textData = item.findViewById(R.id.text_data_anterior_item);
            textData.setText(dia != null ? dfDisplay.format(dia) : dataString);

            item.setOnClickListener(v -> showDialogAnteriores(dia, lista));
            binding.containerAnotacoesAnteriores.addView(item);
        }
    }

    private void showDialogAnteriores(Date data, List<DiarioResponse> lista) {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_anotacoes_anteriores, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_data_anterior_titulo);
        dialogTitle.setText("Anotações de " +
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(data != null ? data : new Date()));

        exibirAnotacoes(dialogView.findViewById(R.id.dialog_container_anotacoes_anteriores), lista, false);

        b.setView(dialogView);
        final AlertDialog alertDialog = b.create();

        dialogView.findViewById(R.id.dialog_btn_fechar_anterior).setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void showNovoSintomaDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialog = getLayoutInflater().inflate(R.layout.dialog_novo_sintoma, null);

        EditText titulo = dialog.findViewById(R.id.edit_text_titulo_sintoma);
        EditText descricao = dialog.findViewById(R.id.edit_text_descricao_sintoma);

        builder.setView(dialog);
        builder.setPositiveButton("Salvar", (d, i) -> {
            String intensidade = titulo.getText().toString().trim();
            String desc = descricao.getText().toString().trim();
            if (intensidade.isEmpty() || desc.isEmpty()) {
                Toast.makeText(getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }
            registrarSintoma(intensidade, desc);
        });
        builder.setNegativeButton("Cancelar", (d, i) -> d.dismiss());
        builder.create().show();
    }

    private void registrarSintoma(String intensidade, String descricao) {
        SimpleDateFormat dfData = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dfData.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        dfHora.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

        Date agora = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo")).getTime();

        String data = dfData.format(agora);
        String horario = dfHora.format(agora);


        DiarioRequest req = new DiarioRequest(data, horario, intensidade, descricao);

        api.registrarSintoma(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> r) {
                if(isAdded()) {
                    Toast.makeText(getContext(), "Sintoma registrado!", Toast.LENGTH_SHORT).show();
                    carregarDiario();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if(isAdded()) {
                    Toast.makeText(getContext(), "Erro ao registrar sintoma.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void salvarAlteracoes() {
        binding.fabEditarSintoma.setEnabled(false);

        final List<DiarioResponse> changedItems = new ArrayList<>();
        final List<DiarioRequest> requests = new ArrayList<>();
        int count = binding.containerAnotacoesHoje.getChildCount();

        for (int i = 0; i < count; i++) {
            View itemView = binding.containerAnotacoesHoje.getChildAt(i);
            DiarioResponse originalResponse = (DiarioResponse) itemView.getTag();
            if (originalResponse == null) continue;

            EditText editTitulo = itemView.findViewById(R.id.edit_titulo_sintoma);
            EditText editDescricao = itemView.findViewById(R.id.edit_descricao_sintoma);

            String intensidade = editTitulo.getText().toString();
            String descricao = editDescricao.getText().toString();

            if (!intensidade.equals(originalResponse.getIntensidade()) || !descricao.equals(originalResponse.getDescricao())) {
                changedItems.add(originalResponse);
                requests.add(new DiarioRequest(originalResponse.getData(), originalResponse.getHorario(), intensidade, descricao));
            }
        }

        if (changedItems.isEmpty()) {
            exitEditMode(null);
            return;
        }

        final AtomicInteger pendingSaves = new AtomicInteger(changedItems.size());
        final List<String> errorMessages = new ArrayList<>();

        for (int i = 0; i < changedItems.size(); i++) {
            DiarioResponse item = changedItems.get(i);
            DiarioRequest request = requests.get(i);

            api.atualizarDiario(item.getId(), request).enqueue(new Callback<DiarioResponse>() {
                @Override
                public void onResponse(Call<DiarioResponse> call, Response<DiarioResponse> response) {
                    if (!response.isSuccessful()) {
                        errorMessages.add("Item " + item.getId());
                    }
                    if (pendingSaves.decrementAndGet() == 0) {
                        exitEditMode(errorMessages);
                    }
                }

                @Override
                public void onFailure(Call<DiarioResponse> call, Throwable t) {
                    errorMessages.add("Item " + item.getId() + " (Falha na conexão)");
                    if (pendingSaves.decrementAndGet() == 0) {
                        exitEditMode(errorMessages);
                    }
                }
            });
        }
    }

    private void exitEditMode(List<String> errors) {
        if (!isAdded()) return;

        isEditMode = false;
        binding.fabEditarSintoma.setEnabled(true);
        binding.fabEditarSintoma.setImageResource(R.drawable.ic_edit);
        binding.fabAdicionarSintoma.setVisibility(View.VISIBLE);

        if (errors == null) {
            // No changes were made, no toast needed
        } else if (errors.isEmpty()) {
            Toast.makeText(getContext(), "Alterações salvas com sucesso!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Falha ao salvar " + errors.size() + " itens.", Toast.LENGTH_LONG).show();
        }

        carregarDiario();
    }

    private void excluirAnotacao(Long id) {
        new AlertDialog.Builder(getContext())
                .setTitle("Excluir Anotação")
                .setMessage("Tem certeza que deseja excluir esta anotação?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    api.deletarDiario(id).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (isAdded() && response.isSuccessful()) {
                                Toast.makeText(getContext(), "Anotação excluída", Toast.LENGTH_SHORT).show();
                                carregarDiario();
                            } else if (isAdded()) {
                                Toast.makeText(getContext(), "Falha ao excluir anotação", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            if (isAdded()) {
                                Toast.makeText(getContext(), "Falha ao excluir anotação", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
