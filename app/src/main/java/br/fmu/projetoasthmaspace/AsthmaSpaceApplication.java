package br.fmu.projetoasthmaspace;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import br.fmu.projetoasthmaspace.Data.worker.NotificacaoScheduler;

public class AsthmaSpaceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        criarCanaisDeNotificacao();
        NotificacaoScheduler.agendarVerificacaoAr(this);
    }

    private void criarCanaisDeNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);

            // Canal qualidade do ar
            manager.createNotificationChannel(new NotificationChannel(
                    "QUALIDADE_AR", "Qualidade do Ar", NotificationManager.IMPORTANCE_HIGH));

            // Canal lembretes (que você já tinha na MainActivity)
            manager.createNotificationChannel(new NotificationChannel(
                    "LEMBRETES", "Lembretes do App", NotificationManager.IMPORTANCE_HIGH));
        }
    }
}