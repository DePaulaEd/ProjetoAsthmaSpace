package br.fmu.projetoasthmaspace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.fmu.projetoasthmaspace.Domain.LembreteResponse;

public class TarefasAdapter extends RecyclerView.Adapter<TarefasAdapter.TarefaViewHolder> {

    public interface OnTarefaConcluidaListener {
        void onTarefaConcluida(LembreteResponse tarefa);
    }

    private List<LembreteResponse> lista;
    private OnTarefaConcluidaListener listener;

    public TarefasAdapter(List<LembreteResponse> lista, OnTarefaConcluidaListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TarefaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarefa_checkbox, parent, false);
        return new TarefaViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull TarefaViewHolder holder, int position) {
        LembreteResponse tarefa = lista.get(position);

        holder.checkbox.setOnCheckedChangeListener(null);

        holder.checkbox.setText(tarefa.titulo + " - " + tarefa.horario);
        holder.checkbox.setChecked(false);

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) listener.onTarefaConcluida(tarefa);
        });
    }


    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class TarefaViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;

        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox_tarefa);
        }
    }
}

