package br.fmu.projetoasthmaspace.Presentation.ActivityView;

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

import br.fmu.projetoasthmaspace.Core.Domain.Diario.DiarioParser;
import br.fmu.projetoasthmaspace.Core.Domain.Lembretes.LembreteResponse;
import br.fmu.projetoasthmaspace.Core.Domain.Lembretes.LembreteUpdateRequest;
import br.fmu.projetoasthmaspace.Core.Util.PaginaResponse;
import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiClient;
import br.fmu.projetoasthmaspace.Data.Service.Client.ApiService;
import br.fmu.projetoasthmaspace.databinding.ActivityTarefasBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TarefasActivity extends Fragment {

    private ActivityTarefasBinding binding;
    private ApiService api;
    private String token;

    private TarefasAdapter adapter;
    private List<LembreteResponse> tarefasHoje = new ArrayList<>();
    private List<LembreteResponse> tarefasConcluidasHoje = new ArrayList<>();

    // Variáveis de controle — adiciona no topo da classe
    private int paginaAtual = 0;
    private boolean carregando = false;
    private boolean ultimaPagina = false;
    private List<LembreteResponse> todosLembretes = new ArrayList<>();

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
//        carregarLembretesDoBackend();
        recarregarLembretes();
    }

    // Substitui carregarLembretesDoBackend()
    private void carregarLembretesDoBackend() {
        if (carregando || ultimaPagina) return;
        carregando = true;

        Log.d("TAREFAS", "Carregando página " + paginaAtual);

        api.listarLembretes(paginaAtual, 20).enqueue(new Callback<PaginaResponse<LembreteResponse>>() {
            @Override
            public void onResponse(Call<PaginaResponse<LembreteResponse>> call,
                                   Response<PaginaResponse<LembreteResponse>> response) {
                if (!isAdded() || binding == null) return;
                carregando = false;

                if (response.isSuccessful() && response.body() != null) {
                    PaginaResponse<LembreteResponse> pagina = response.body();

                    todosLembretes.addAll(pagina.getContent());
                    ultimaPagina = pagina.isLast();
                    paginaAtual++;

                    Log.d("TAREFAS", "Página " + pagina.getNumber() + " carregada. Total acumulado: " + todosLembretes.size());

                    filtrarTarefasDeHoje(todosLembretes);
                } else {
                    Log.e("TAREFAS", "Erro HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PaginaResponse<LembreteResponse>> call, Throwable t) {
                carregando = false;
                Log.e("TAREFAS", "Falha na requisição: " + t.getMessage());
            }
        });
    }

    // Adiciona esse método para resetar após marcar/desmarcar concluído
    private void recarregarLembretes() {
        paginaAtual = 0;
        ultimaPagina = false;
        carregando = false;
        todosLembretes = new ArrayList<>();
        carregarLembretesDoBackend();
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

            checkbox.setText(t.getHorarioFormatado() + " — " + t.titulo);
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
