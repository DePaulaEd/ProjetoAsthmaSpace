package br.fmu.projetoasthmaspace.Data.worker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

public class NotificacaoScheduler {

    private static final String TAG = "NotificacaoScheduler";

    // Janelas de verificação: 7h, 10h, 13h, 16h, 19h, 22h
    private static final int[] JANELAS = {7, 10, 13, 16, 19, 22};

    /**
     * Chamado uma vez no Application.onCreate para garantir que
     * sempre existe um alarme agendado ao iniciar o processo do app.
     */
    public static void agendarVerificacaoAr(Context context) {
        agendarProximoAlarme(context);
    }

    /**
     * Agenda o próximo alarme da sequência (janelas fixas de 7h às 22h).
     * Chamado pelo QualidadeArWorker ao final de doWork() — nunca pelo
     * QualidadeArReceiver — para que o próximo disparo seja calculado
     * apenas após a conclusão da verificação atual.
     */
    public static void agendarProximoAlarme(Context context) {
        long proximoDisparo = calcularProximoDisparo();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = criarPendingIntent(context);

        if (alarmManager == null) return;

        // Android 12+: verifica se pode agendar alarmes exatos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Sem permissão para alarmes exatos. Usando inexato.");
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        proximoDisparo,
                        pendingIntent
                );
                return;
            }
        }

        // Alarme exato — funciona mesmo em Doze Mode
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                proximoDisparo,
                pendingIntent
        );

        Log.d(TAG, "Próximo alarme agendado para: " + new java.util.Date(proximoDisparo));
    }

    /**
     * Cancela todos os alarmes agendados (útil ao fazer logout).
     */
    public static void cancelarAlarmes(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(criarPendingIntent(context));
            Log.d(TAG, "Alarmes cancelados.");
        }
    }

    private static PendingIntent criarPendingIntent(Context context) {
        Intent intent = new Intent(context, QualidadeArReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, 0, intent, flags);
    }

    private static long calcularProximoDisparo() {
        long agora = System.currentTimeMillis();
        // Folga de 2 min: garante que nunca agendamos para "agora" ou para o passado,
        // mesmo que este método rode segundos após o disparo do alarme atual
        long margemSeguranca = 2 * 60 * 1000L;

        TimeZone tzSp = TimeZone.getTimeZone("America/Sao_Paulo");

        for (int janela : JANELAS) {
            Calendar candidato = Calendar.getInstance(tzSp);
            candidato.set(Calendar.HOUR_OF_DAY, janela);
            candidato.set(Calendar.MINUTE, 0);
            candidato.set(Calendar.SECOND, 0);
            candidato.set(Calendar.MILLISECOND, 0);

            if (candidato.getTimeInMillis() > agora + margemSeguranca) {
                return candidato.getTimeInMillis();
            }
        }

        // Todas as janelas de hoje já passaram — amanhã às 7h
        Calendar amanha = Calendar.getInstance(tzSp);
        amanha.add(Calendar.DAY_OF_MONTH, 1);
        amanha.set(Calendar.HOUR_OF_DAY, 7);
        amanha.set(Calendar.MINUTE, 0);
        amanha.set(Calendar.SECOND, 0);
        amanha.set(Calendar.MILLISECOND, 0);
        return amanha.getTimeInMillis();
    }
}