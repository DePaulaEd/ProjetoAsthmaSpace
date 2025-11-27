package br.fmu.projetoasthmaspace;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.fmu.projetoasthmaspace.Domain.LembreteResponse;

public class LembretesAdapter extends RecyclerView.Adapter<LembretesAdapter.LembreteViewHolder> {

    // Interface para callbacks
    public interface OnLembreteListener {
        void onLembreteConcluido(LembreteResponse lembrete);
        void onLembreteRemover(LembreteResponse lembrete);
    }

    private List<LembreteResponse> lista;
    private OnLembreteListener listener;

    public LembretesAdapter(List<LembreteResponse> lista, OnLembreteListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LembreteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lembrete, parent, false); // criar layout item_lembrete.xml
        return new LembreteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LembreteViewHolder holder, int position) {
        LembreteResponse lembrete = lista.get(position);

        holder.checkbox.setOnCheckedChangeListener(null); // evita múltiplos listeners
        holder.checkbox.setChecked(lembrete.concluido); // se você tem boolean concluido
        holder.checkbox.setText(lembrete.titulo + " - " + lembrete.horario);

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            lembrete.concluido = isChecked;
            if (listener != null) listener.onLembreteConcluido(lembrete);
        });

        holder.btnLixeira.setOnClickListener(v -> {
            if (listener != null) listener.onLembreteRemover(lembrete);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class LembreteViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        Button btnLixeira;

        public LembreteViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox_lembrete);
            btnLixeira = itemView.findViewById(R.id.btn_lixeira);
        }
    }
}
