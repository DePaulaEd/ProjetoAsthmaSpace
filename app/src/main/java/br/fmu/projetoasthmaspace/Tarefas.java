package br.fmu.projetoasthmaspace;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import br.fmu.projetoasthmaspace.Domain.LembreteResponse;
import br.fmu.projetoasthmaspace.Domain.LembreteUpdateRequest;
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
        api = ApiClient.getApiService(token);

        binding.recyclerTarefas.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarLembretesDoBackend();
    }

    private void carregarLembretesDoBackend() {
        api.listarLembretes().enqueue(new Callback<List<LembreteResponse>>() {
            @Override
            public void onResponse(Call<List<LembreteResponse>> call, Response<List<LembreteResponse>> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    filtrarTarefasDeHoje(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<LembreteResponse>> call, Throwable t) { }
        });
    }

    private void filtrarTarefasDeHoje(List<LembreteResponse> lista) {

        String hoje = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new java.util.Date());

        tarefasHoje = lista.stream()
                .filter(l -> !l.concluido &&
                        l.data != null &&
                        l.data.startsWith(hoje))
                .collect(Collectors.toList());

        tarefasConcluidasHoje = lista.stream()
                .filter(l -> l.concluido &&
                        l.data != null &&
                        l.data.startsWith(hoje))
                .collect(Collectors.toList());

        atualizarUI();
    }


    private void atualizarUI() {

        if (!isAdded() || binding == null) return;

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

            View item = inflater.inflate(R.layout.item_tarefa_concluida, binding.containerConcluidas, false);
            CheckBox checkbox = item.findViewById(R.id.checkbox_concluida);

            checkbox.setText(t.horario + " — " + t.titulo);
            checkbox.setChecked(true);

            // texto riscado
            checkbox.setPaintFlags(
                    checkbox.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            );

            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isChecked) {

                    // muda no modelo local
                    t.concluido = false;
                    tarefasConcluidasHoje.remove(t);
                    tarefasHoje.add(t);

                    atualizarUI();

                    // envia para API
                    LembreteUpdateRequest req = new LembreteUpdateRequest(t.id, false);
                    api.atualizarConclusao(req).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) { }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) { }
                    });
                }
            });

            binding.containerConcluidas.addView(item);
        }
    }

    private void marcarComoConcluida(LembreteResponse tarefa) {

        tarefa.concluido = true;

        // atualiza local
        tarefasHoje.remove(tarefa);
        tarefasConcluidasHoje.add(tarefa);

        atualizarUI();

        // envia ao backend
        LembreteUpdateRequest req = new LembreteUpdateRequest(tarefa.id, true);

        api.atualizarConclusao(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) { }

            @Override
            public void onFailure(Call<Void> call, Throwable t) { }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
