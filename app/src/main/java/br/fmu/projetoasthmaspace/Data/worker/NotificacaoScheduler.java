package br.fmu.projetoasthmaspace.Data.worker;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificacaoScheduler {

    /**
     * Agenda o Worker para rodar a cada 8 horas (aprox. 7h, 13h, 19h).
     * Chame isso no onCreate da MainActivity.
     */
    public static void agendarVerificacaoAr(Context context) {

        // Calcula delay até a próxima janela (7h, 13h ou 19h)
        long delayInicial = calcularDelayParaProximaJanela();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                QualidadeArWorker.class,
                8, TimeUnit.HOURS   // repete a cada 8h
        )
                .setInitialDelay(delayInicial, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "qualidade_ar_worker",
                ExistingPeriodicWorkPolicy.KEEP, // não reagenda se já existe
                workRequest
        );
    }

    private static long calcularDelayParaProximaJanela() {
        Calendar agora = Calendar.getInstance();
        int horaAtual = agora.get(Calendar.HOUR_OF_DAY);

        // Janelas: 7h, 13h, 19h
        int[] janelas = {7, 13, 19};
        int proximaJanela = 7; // padrão: amanhã às 7h

        for (int janela : janelas) {
            if (horaAtual < janela) {
                proximaJanela = janela;
                break;
            }
        }

        Calendar proxima = Calendar.getInstance();
        proxima.set(Calendar.HOUR_OF_DAY, proximaJanela);
        proxima.set(Calendar.MINUTE, 0);
        proxima.set(Calendar.SECOND, 0);
        proxima.set(Calendar.MILLISECOND, 0);

        // Se já passou de 19h, agenda para amanhã às 7h
        if (proxima.before(agora)) {
            proxima.add(Calendar.DAY_OF_MONTH, 1);
            proxima.set(Calendar.HOUR_OF_DAY, 7);
        }

        return proxima.getTimeInMillis() - agora.getTimeInMillis();
    }
}
