package br.fmu.projetoasthmaspace;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import br.fmu.projetoasthmaspace.Domain.DiarioParser;
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
    private List<LembreteResponse> tarefasHoje;

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

    /**
     * 🔄 Carrega lembretes da API
     */
    private void carregarLembretesDoBackend() {
        api.listarLembretes().enqueue(new Callback<List<LembreteResponse>>() {
            @Override
            public void onResponse(Call<List<LembreteResponse>> call, Response<List<LembreteResponse>> response) {
                if (response.isSuccessful()) {
                    List<LembreteResponse> todos = response.body();
                    filtrarTarefasDeHoje(todos);
                }
            }

            @Override
            public void onFailure(Call<List<LembreteResponse>> call, Throwable t) { }
        });
    }

    /**
     * 🔍 Filtra tarefas do dia (não concluídas)
     */
    private void filtrarTarefasDeHoje(List<LembreteResponse> lista) {

        String hoje = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new java.util.Date());

        tarefasHoje = lista.stream()
                .filter(l -> !l.concluido && hoje.equals(l.data)) // compara strings de data
                .collect(Collectors.toList());

        atualizarUI();
    }

//    private void filtrarTarefasDeHoje(List<LembreteResponse> lista) {
//
//        tarefasHoje = lista.stream()
//                .filter(l -> !l.concluido) // apenas não concluídos
//                .collect(Collectors.toList());
//
//        atualizarUI();
//    }



    /**
     * Atualiza o contador e o RecyclerView
     */
    private void atualizarUI() {

        int n = tarefasHoje.size();
        binding.contadorTarefas.setText(
                n == 1 ? "1 tarefa pendente" :
                        String.format(Locale.getDefault(), "%d tarefas pendentes", n)
        );

        adapter = new TarefasAdapter(tarefasHoje, this::marcarComoConcluida);
        binding.recyclerTarefas.setAdapter(adapter);
    }

    /**
     * ✔ Marca tarefa como concluída
     */
    private void marcarComoConcluida(LembreteResponse tarefa) {

        LembreteUpdateRequest req = new LembreteUpdateRequest(tarefa.id, true);

        api.atualizarConclusao(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    tarefa.concluido = true;
                    tarefasHoje.remove(tarefa);
                    adapter.notifyDataSetChanged();
                    atualizarUI();
                }
            }

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
