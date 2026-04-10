package br.fmu.projetoasthmaspace.Presentation.ActivityView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.fmu.projetoasthmaspace.Data.Local.NotificacaoDatabase;
import br.fmu.projetoasthmaspace.Data.Local.NotificacaoEntity;
import br.fmu.projetoasthmaspace.R;

public class NotificacoesActivity extends AppCompatActivity {

    private LinearLayout containerNotificacoes;
    private TextView txtVazio;
    private ImageButton btnMarcarTodas;
    private ImageButton btnExcluirTodas;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacoes);

        containerNotificacoes = findViewById(R.id.containerNotificacoes);
        txtVazio = findViewById(R.id.txtVazio);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        btnMarcarTodas  = findViewById(R.id.btnMarcarTodas);
        btnExcluirTodas = findViewById(R.id.btnExcluirTodas);

        btnMarcarTodas.setOnClickListener(v -> {
            btnMarcarTodas.setColorFilter(
                    getResources().getColor(R.color.green_dark, getTheme()),
                    android.graphics.PorterDuff.Mode.SRC_IN);
            btnMarcarTodas.setEnabled(false);
            marcarTodasComoLidas();
        });

        btnExcluirTodas.setOnClickListener(v -> {
            btnExcluirTodas.setColorFilter(
                    android.graphics.Color.parseColor("#EF5350"),
                    android.graphics.PorterDuff.Mode.SRC_IN);
            btnExcluirTodas.setEnabled(false);
            excluirTodasNotificacoes();
        });

        carregarNotificacoes();
    }

    private void carregarNotificacoes() {
        executor.execute(() -> {
            List<NotificacaoEntity> lista = NotificacaoDatabase
                    .getInstance(this).dao().listarTodas();
            runOnUiThread(() -> renderizarLista(lista));
        });
    }

    private void renderizarLista(List<NotificacaoEntity> lista) {
        containerNotificacoes.removeAllViews();

        if (lista.isEmpty()) {
            txtVazio.setVisibility(View.VISIBLE);
            return;
        }

        txtVazio.setVisibility(View.GONE);

        for (NotificacaoEntity notif : lista) {
            View item = LayoutInflater.from(this)
                    .inflate(R.layout.item_notificacao, containerNotificacoes, false);

            TextView txtTitulo   = item.findViewById(R.id.txtTituloNotif);
            TextView txtMensagem = item.findViewById(R.id.txtMensagemNotif);
            TextView txtDataHora = item.findViewById(R.id.txtDataHoraNotif);
            TextView txtTipo     = item.findViewById(R.id.txtTipoNotif);
            ImageView btnLida    = item.findViewById(R.id.btnMarcarLida);
            ImageView btnDeletar = item.findViewById(R.id.btnDeletarNotif);

            txtTitulo.setText(notif.titulo);
            txtMensagem.setText(notif.mensagem);
            txtDataHora.setText(notif.dataHora);
            txtTipo.setText("AR".equals(notif.tipo) ? "🌫️ Qualidade do Ar" : "🔔 Lembrete");

            item.setAlpha(notif.lida ? 0.6f : 1.0f);

            btnLida.setImageResource(notif.lida
                    ? android.R.drawable.checkbox_on_background
                    : android.R.drawable.checkbox_off_background);
            btnLida.setColorFilter(
                    notif.lida
                            ? getResources().getColor(R.color.green_dark, getTheme())
                            : android.graphics.Color.parseColor("#4FC3F7"),
                    android.graphics.PorterDuff.Mode.SRC_IN);

            btnLida.setOnClickListener(v -> {
                if (!notif.lida) {
                    btnLida.setImageResource(android.R.drawable.checkbox_on_background);
                    btnLida.setColorFilter(
                            getResources().getColor(R.color.green_dark, getTheme()),
                            android.graphics.PorterDuff.Mode.SRC_IN);
                    btnLida.setEnabled(false);
                    btnLida.postDelayed(() -> executor.execute(() -> {
                        NotificacaoDatabase.getInstance(this).dao().marcarComoLida(notif.id);
                        runOnUiThread(this::carregarNotificacoes);
                    }), 500);
                }
            });

            btnDeletar.setOnClickListener(v -> {
                btnDeletar.setColorFilter(
                        android.graphics.Color.parseColor("#EF5350"),
                        android.graphics.PorterDuff.Mode.SRC_IN);
                btnDeletar.setEnabled(false);
                btnDeletar.postDelayed(() -> executor.execute(() -> {
                    NotificacaoDatabase.getInstance(this).dao().deletar(notif.id);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Notificação removida", Toast.LENGTH_SHORT).show();
                        carregarNotificacoes();
                    });
                }), 500);
            });

            containerNotificacoes.addView(item);
        }
    }

    private void marcarTodasComoLidas() {
        for (int i = 0; i < containerNotificacoes.getChildCount(); i++) {
            View item = containerNotificacoes.getChildAt(i);
            ImageView btnLida = item.findViewById(R.id.btnMarcarLida);
            if (btnLida != null) {
                btnLida.setImageResource(android.R.drawable.checkbox_on_background);
                btnLida.setColorFilter(
                        getResources().getColor(R.color.green_dark, getTheme()),
                        android.graphics.PorterDuff.Mode.SRC_IN);
                btnLida.setEnabled(false);
            }
        }

        containerNotificacoes.postDelayed(() -> executor.execute(() -> {
            NotificacaoDatabase.getInstance(this).dao().marcarTodasComoLidas();
            runOnUiThread(() -> {
                btnMarcarTodas.setEnabled(true);
                btnMarcarTodas.clearColorFilter();
                Toast.makeText(this, "Todas marcadas como lidas", Toast.LENGTH_SHORT).show();
                carregarNotificacoes();
            });
        }), 500);
    }

    private void excluirTodasNotificacoes() {
        for (int i = 0; i < containerNotificacoes.getChildCount(); i++) {
            View item = containerNotificacoes.getChildAt(i);
            ImageView btnDeletar = item.findViewById(R.id.btnDeletarNotif);
            if (btnDeletar != null) {
                btnDeletar.setColorFilter(
                        android.graphics.Color.parseColor("#EF5350"),
                        android.graphics.PorterDuff.Mode.SRC_IN);
                btnDeletar.setEnabled(false);
            }
        }

        containerNotificacoes.postDelayed(() -> executor.execute(() -> {
            NotificacaoDatabase.getInstance(this).dao().deletarTodas();
            runOnUiThread(() -> {
                btnExcluirTodas.setEnabled(true);
                btnExcluirTodas.clearColorFilter();
                Toast.makeText(this, "Todas as notificações removidas", Toast.LENGTH_SHORT).show();
                carregarNotificacoes();
            });
        }), 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}