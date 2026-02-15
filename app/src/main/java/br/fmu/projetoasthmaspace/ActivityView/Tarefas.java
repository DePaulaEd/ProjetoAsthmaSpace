package br.fmu.projetoasthmaspace.ActivityView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;  // <--- IMPORTANTE
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

import br.fmu.projetoasthmaspace.Domain.DiarioParser;
import br.fmu.projetoasthmaspace.Domain.LembreteResponse;
import br.fmu.projetoasthmaspace.Domain.LembreteUpdateRequest;
import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.Service.ApiClient;
import br.fmu.projetoasthmaspace.Service.ApiService;
import br.fmu.projetoasthmaspace.databinding.ActivityTarefasBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Tarefas extends Fragment {

    private ActivityTarefasBinding binding;
    private ApiService api;
    private String token;

    private TarefasAdapter adapter;
    private List<LembreteResponse> tarefasHoje = new ArrayList<>();
    private List<LembreteResponse> tarefasConcluidasHoje = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityTarefasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("APP", Context.MODE_PRIVATE);
        token = prefs.getString("TOKEN", null);
        api = ApiClient.getApiService(requireContext());





        binding.recyclerTarefas.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarLembretesDoBackend();
    }

    private void carregarLembretesDoBackend() {

        Log.d("TAREFAS", "Chamando API listarLembretes()...");

        api.listarLembretes().enqueue(new Callback<List<LembreteResponse>>() {
            @Override
            public void onResponse(Call<List<LembreteResponse>> call, Response<List<LembreteResponse>> response) {

                Log.d("TAREFAS", "Resposta recebida. Sucesso? " + response.isSuccessful());

                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {

                    List<LembreteResponse> lista = response.body();

                    Log.d("TAREFAS", "Total de lembretes recebidos: " + lista.size());

                    for (LembreteResponse l : lista) {
                        Log.d("TAREFAS", "ITEM -> id=" + l.id +
                                " data=" + l.data +
                                " horario=" + l.horario +
                                " concluído=" + l.concluido);
                    }

                    filtrarTarefasDeHoje(lista);

                } else {
                    Log.e("TAREFAS", "Erro HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<LembreteResponse>> call, Throwable t) {
                Log.e("TAREFAS", "Falha na requisição: " + t.getMessage());
            }
        });
    }

    private void filtrarTarefasDeHoje(List<LembreteResponse> lista) {

        // Calcula hoje corretamente no fuso do Brasil
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        String hoje = sdf.format(new Date());

        Log.d("TAREFAS", "Data de HOJE calculada: " + hoje);

        tarefasHoje = lista.stream()
                .filter(l -> !l.concluido &&
                        l.data != null &&
                        DiarioParser.isToday(l.data))
                .collect(Collectors.toList());

        tarefasConcluidasHoje = lista.stream()
                .filter(l -> l.concluido &&
                        l.data != null &&
                        DiarioParser.isToday(l.data))
                .collect(Collectors.toList());

        Log.d("TAREFAS", "→ Pendentes hoje: " + tarefasHoje.size());
        Log.d("TAREFAS", "→ Concluídas hoje: " + tarefasConcluidasHoje.size());

        atualizarUI();
    }



    private void atualizarUI() {

        if (!isAdded() || binding == null) return;

        Log.d("TAREFAS", "Atualizando UI... Pendentes: " + tarefasHoje.size());

        int n = tarefasHoje.size();
        binding.contadorTarefas.setText(
                n == 1 ? "1 tarefa pendente" :
                        String.format(Locale.getDefault(), "%d tarefas pendentes", n)
        );

        adapter = new TarefasAdapter(tarefasHoje, this::marcarComoConcluida);
        binding.recyclerTarefas.setAdapter(adapter);

        atualizarConcluidasHoje();
    }

    private void atualizarConcluidasHoje() {

        if (!isAdded() || binding == null) return;

        Log.d("TAREFAS", "Renderizando concluídas: " + tarefasConcluidasHoje.size());

        if (tarefasConcluidasHoje.isEmpty()) {
            binding.tituloConcluidas.setVisibility(View.GONE);
            binding.containerConcluidas.setVisibility(View.GONE);
            return;
        }

        binding.tituloConcluidas.setVisibility(View.VISIBLE);
        binding.containerConcluidas.setVisibility(View.VISIBLE);

        binding.containerConcluidas.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (LembreteResponse t : tarefasConcluidasHoje) {

            Log.d("TAREFAS", "Exibindo concluída id=" + t.id);

            View item = inflater.inflate(R.layout.item_tarefa_concluida, binding.containerConcluidas, false);
            CheckBox checkbox = item.findViewById(R.id.checkbox_concluida);

            checkbox.setText(t.horario + " — " + t.titulo);
            checkbox.setChecked(true);

            checkbox.setPaintFlags(
                    checkbox.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            );

            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isChecked) {

                    Log.d("TAREFAS", "Desmarcando concluída id=" + t.id);

                    t.concluido = false;
                    tarefasConcluidasHoje.remove(t);
                    tarefasHoje.add(t);

                    atualizarUI();

                    LembreteUpdateRequest req = new LembreteUpdateRequest(false);

                    api.atualizarDados(t.id, req)
                            .enqueue(new Callback<Void>() {
                                @Override public void onResponse(Call<Void> call, Response<Void> response) { }
                                @Override public void onFailure(Call<Void> call, Throwable t) { }
                            });

                }
            });

            binding.containerConcluidas.addView(item);
        }
    }

    private void marcarComoConcluida(LembreteResponse tarefa) {

        Log.d("TAREFAS", "Marcando como concluída id=" + tarefa.id);

        tarefa.concluido = true;

        tarefasHoje.remove(tarefa);
        tarefasConcluidasHoje.add(tarefa);

        atualizarUI();

        LembreteUpdateRequest req = new LembreteUpdateRequest(true);

        api.atualizarDados(tarefa.id, req)
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> call, Response<Void> response) { }
                    @Override public void onFailure(Call<Void> call, Throwable t) { }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
