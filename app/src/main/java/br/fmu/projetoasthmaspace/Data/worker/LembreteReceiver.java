package br.fmu.projetoasthmaspace.Data.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import br.fmu.projetoasthmaspace.R;

public class LembreteReceiver extends BroadcastReceiver {

    private static final String CANAL_ID        = "LEMBRETES";
    private static final String PREFS_CONFIG    = "CONFIG";
    private static final String KEY_LEMBRETES   = "lembretes_ativos";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Verifica se lembretes estão ativados nas configurações
        SharedPreferences prefs = context.getSharedPreferences(PREFS_CONFIG, Context.MODE_PRIVATE);
        boolean lembretesAtivos = prefs.getBoolean(KEY_LEMBRETES, true);

        if (!lembretesAtivos) return; // silenciosamente ignora se desativado

        String titulo   = intent.getStringExtra("titulo");
        String mensagem = intent.getStringExtra("mensagem");

        if (titulo == null || mensagem == null) return;

        // Dispara notificação do sistema
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    "Lembretes de Medicação",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(canal);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CANAL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(titulo)
                        .setContentText(mensagem)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}