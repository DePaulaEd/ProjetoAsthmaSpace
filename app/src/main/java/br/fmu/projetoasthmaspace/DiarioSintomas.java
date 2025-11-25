package br.fmu.projetoasthmaspace;

import android.content.Context;
import android.content.DialogInterface;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import br.fmu.projetoasthmaspace.Domain.DiarioParser;
import br.fmu.projetoasthmaspace.Domain.DiarioRequest;
import br.fmu.projetoasthmaspace.Domain.DiarioResponse;
import br.fmu.projetoasthmaspace.Service.ApiClient;
import br.fmu.projetoasthmaspace.Service.ApiService;
import br.fmu.projetoasthmaspace.databinding.ActivityDiarioSintomasBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiarioSintomas extends Fragment {

    private ActivityDiarioSintomasBinding binding;
    private List<DiarioResponse> diario;
    private String token;
    private ApiService api;

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
        api = ApiClient.getApiService(token);

        binding.fabAdicionarSintoma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNovoSintomaDialog();
            }
        });

        carregarDiario();
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
        binding.textDataAtual.setText(dfHoje.format(new Date()));

        List<DiarioResponse> hoje = new ArrayList<>();
        Map<String, List<DiarioResponse>> anteriores = new HashMap<>();
        for (DiarioResponse d : diario) {
            if (DiarioParser.isToday(d.data)) {
                hoje.add(d);
            } else {
                if (!anteriores.containsKey(d.data)) {
                    anteriores.put(d.data, new ArrayList<>());
                }
                anteriores.get(d.data).add(d);
            }
        }

        exibirAnotacoes(binding.containerAnotacoesHoje, hoje);
        exibirDatasAnteriores(anteriores);
    }

    private void exibirAnotacoes(LinearLayout container, List<DiarioResponse> lista) {
        container.removeAllViews();
        if (getContext() == null) return;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (final DiarioResponse resp : lista) {
            View itemView = inflater.inflate(R.layout.item_anotacao_diario, container, false);

            TextView itemHorario = itemView.findViewById(R.id.item_horario);
            TextView itemTitulo = itemView.findViewById(R.id.item_titulo);
            TextView itemDescricao = itemView.findViewById(R.id.item_descricao);

            final Date hora = DiarioParser.parseHorario(resp.horario);
            itemHorario.setText(hora != null ? formatoHora.format(hora) : resp.horario);
            itemTitulo.setText("Intensidade: " + resp.intensidade);
            
            // CORREÇÃO: Adicionando verificação de nulo para a descrição na lista
            itemDescricao.setText(resp.descricao != null ? resp.descricao : "");

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String horarioFormatado = (hora != null) ? formatoHora.format(hora) : resp.horario;
                    String tituloDialogo = "Intensidade: " + resp.intensidade;
                    
                    // CORREÇÃO: Adicionando verificação de nulo para a mensagem do diálogo
                    String descricaoFormatada = (resp.descricao != null && !resp.descricao.isEmpty()) ? resp.descricao : "(Nenhuma descrição fornecida)";
                    String mensagemDialogo = "Anotado às: " + horarioFormatado + "\n\n" + descricaoFormatada;

                    new AlertDialog.Builder(getContext())
                            .setTitle(tituloDialogo)
                            .setMessage(mensagemDialogo)
                            .setPositiveButton("Fechar", null)
                            .show();
                }
            });

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

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialogAnteriores(dia, lista);
                }
            });
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

        exibirAnotacoes(dialogView.findViewById(R.id.dialog_container_anotacoes_anteriores), lista);

        b.setView(dialogView);
        final AlertDialog alertDialog = b.create();

        dialogView.findViewById(R.id.dialog_btn_fechar_anterior).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void showNovoSintomaDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialog = getLayoutInflater().inflate(R.layout.dialog_novo_sintoma, null);

        EditText titulo = dialog.findViewById(R.id.edit_text_titulo_sintoma);
        EditText descricao = dialog.findViewById(R.id.edit_text_descricao_sintoma);

        builder.setView(dialog);
        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int i) {
                String intensidade = titulo.getText().toString().trim();
                String desc = descricao.getText().toString().trim();
                if (intensidade.isEmpty() || desc.isEmpty()) {
                    Toast.makeText(getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                registrarSintoma(intensidade, desc);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int i) {
                d.dismiss();
            }
        });
        builder.create().show();
    }

    private void registrarSintoma(String intensidade, String descricao) {
        String data = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String horario = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
