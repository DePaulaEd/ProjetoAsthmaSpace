package br.fmu.projetoasthmaspace;

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
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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

        // PEGAR TOKEN AQUI
        SharedPreferences prefs = requireActivity().getSharedPreferences("APP", Context.MODE_PRIVATE);
        token = prefs.getString("TOKEN", null);



        // INSTANCIAR API COM TOKEN
        api = ApiClient.getApiService(token);

        binding.fabAdicionarSintoma.setOnClickListener(v -> showNovoSintomaDialog());

        carregarDiario();
    }

    private void carregarDiario() {
        api.listarDiario().enqueue(new Callback<List<DiarioResponse>>() {
            @Override
            public void onResponse(Call<List<DiarioResponse>> call, Response<List<DiarioResponse>> response) {
                if (!isAdded()) return; // Fragment não está anexada
                if (response.isSuccessful()) {
                    diario = response.body();
                    atualizarTela();
                }
            }

            @Override
            public void onFailure(Call<List<DiarioResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Erro ao carregar diário", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void atualizarTela() {
        if (diario == null || binding == null) return;

        SimpleDateFormat dfHoje = new SimpleDateFormat("dd 'de' MMMM", Locale.getDefault());
        binding.textDataAtual.setText(dfHoje.format(new Date()));

        List<DiarioResponse> hoje = diario.stream()
                .filter(d -> DiarioParser.isToday(d.data))
                .collect(Collectors.toList());

        Map<String, List<DiarioResponse>> anteriores = diario.stream()
                .filter(d -> !DiarioParser.isToday(d.data))
                .collect(Collectors.groupingBy(d -> d.data));

        exibirAnotacoes(binding.containerAnotacoesHoje, hoje);
        exibirDatasAnteriores(anteriores);
    }




    private void exibirAnotacoes(LinearLayout container, List<DiarioResponse> lista) {
        container.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (DiarioResponse resp : lista) {
            View itemView = inflater.inflate(R.layout.item_anotacao_diario, container, false);

            Date hora = DiarioParser.parseHorario(resp.horario);

            ((TextView) itemView.findViewById(R.id.item_horario))
                    .setText(hora != null ? formatoHora.format(hora) : resp.horario);

            ((TextView) itemView.findViewById(R.id.item_titulo)).setText(resp.intensidade);
            ((TextView) itemView.findViewById(R.id.item_descricao)).setText(resp.descricao);

            container.addView(itemView);
        }
    }


    private void exibirDatasAnteriores(Map<String, List<DiarioResponse>> map) {

        binding.containerAnotacoesAnteriores.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        SimpleDateFormat dfDisplay = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault());

        for (Map.Entry<String, List<DiarioResponse>> entry : map.entrySet()) {

            String dataString = entry.getKey(); // yyyy-MM-dd
            List<DiarioResponse> lista = entry.getValue();

            Date dia = DiarioParser.parseData(dataString);

            View item = inflater.inflate(R.layout.item_data_anterior, binding.containerAnotacoesAnteriores, false);

            ((TextView) item.findViewById(R.id.text_data_anterior_item))
                    .setText(dia != null ? dfDisplay.format(dia) : dataString);

            item.setOnClickListener(v -> showDialogAnteriores(dia, lista));

            binding.containerAnotacoesAnteriores.addView(item);
        }
    }


    private void showDialogAnteriores(Date data, List<DiarioResponse> lista) {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        View dialog = getLayoutInflater().inflate(R.layout.dialog_anotacoes_anteriores, null);

        ((TextView) dialog.findViewById(R.id.dialog_data_anterior_titulo))
                .setText("Anotações de " +
                        new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(data != null ? data : new Date()));

        exibirAnotacoes(dialog.findViewById(R.id.dialog_container_anotacoes_anteriores), lista);

        b.setView(dialog);
        AlertDialog d = b.create();

        dialog.findViewById(R.id.dialog_btn_fechar_anterior).setOnClickListener(v -> d.dismiss());

        d.show();
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
        String data = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String horario = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        DiarioRequest req = new DiarioRequest(data, horario, intensidade, descricao);

        api.registrarSintoma(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> r) {
                Toast.makeText(getContext(), "Sintoma registrado!", Toast.LENGTH_SHORT).show();
                carregarDiario();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Erro ao registrar sintoma.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
