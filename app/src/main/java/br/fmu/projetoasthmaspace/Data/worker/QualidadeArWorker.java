package br.fmu.projetoasthmaspace.Data.worker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Tasks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.fmu.projetoasthmaspace.Data.Local.NotificacaoDatabase;
import br.fmu.projetoasthmaspace.Data.Local.NotificacaoEntity;
import br.fmu.projetoasthmaspace.Core.Util.AirQualityUtils;
import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.Data.Service.QualityAir.AirResponse;
import br.fmu.projetoasthmaspace.Data.Service.QualityAir.ApiOpenWeather;

public class QualidadeArWorker extends Worker {

    private static final String TAG      = "QualidadeArWorker";
    private static final String CANAL_ID = "QUALIDADE_AR";

    // Coordenadas padrão (São Paulo) — usado se GPS não disponível
    private static final double LAT_PADRAO = -23.5505;
    private static final double LON_PADRAO = -46.6333;

    public QualidadeArWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // 1 — Obtém coordenadas (GPS ou padrão)
            double[] coords = obterCoordenadas();
            double lat = coords[0];
            double lon = coords[1];
            Log.d(TAG, "Coordenadas: " + lat + ", " + lon);

            // 2 — Chamada síncrona usando ApiOpenWeather já existente no projeto
            retrofit2.Response<AirResponse> response = ApiOpenWeather
                    .getApiService()
                    .getAirQuality(lat, lon)
                    .execute();

            if (!response.isSuccessful() || response.body() == null
                    || response.body().list == null
                    || response.body().list.isEmpty()) {
                Log.e(TAG, "Resposta inválida: " + response.code());
                return Result.retry();
            }

            int aqi = response.body().list.get(0).main.aqi;


            String titulo   = getTitulo(aqi);
            String mensagem = AirQualityUtils.gerarRecomendacaoAqi(converterAqiParaUs(aqi));


            String dataHora   = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR")).format(new Date());
            String dataPrefix = new SimpleDateFormat("dd/MM/yyyy",        new Locale("pt", "BR")).format(new Date());

            NotificacaoDatabase db = NotificacaoDatabase.getInstance(getApplicationContext());


            if (aqi >= 3 || db.dao().contarNotificacoesArNaData(dataPrefix) == 0) {
                db.dao().inserir(new NotificacaoEntity(titulo, mensagem, dataHora, "AR"));
                dispararNotificacaoSistema(titulo, mensagem);
                Log.d(TAG, "Notificação salva — AQI: " + aqi);
            } else {
                Log.d(TAG, "Ar bom e já notificou hoje, pulando.");
            }

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Erro no worker: " + e.getMessage());
            return Result.retry();
        }
    }

    // Obtém última localização conhecida — Tasks.await() é seguro em Worker (background thread)
    private double[] obterCoordenadas() {
        Context ctx = getApplicationContext();

        boolean temPermissao =
                ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED;

        if (!temPermissao) {
            Log.w(TAG, "Sem permissão de localização, usando padrão.");
            return new double[]{LAT_PADRAO, LON_PADRAO};
        }

        try {
            FusedLocationProviderClient client =
                    LocationServices.getFusedLocationProviderClient(ctx);

            android.location.Location location = Tasks.await(client.getLastLocation());

            if (location != null) {
                return new double[]{location.getLatitude(), location.getLongitude()};
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter localização: " + e.getMessage());
        }

        Log.w(TAG, "Localização null, usando padrão.");
        return new double[]{LAT_PADRAO, LON_PADRAO};
    }

    // Converte escala OpenWeatherMap (1-5) para US AQI (0-300+)
    // necessário porque AirQualityUtils.gerarRecomendacaoAqi() usa escala US
    private int converterAqiParaUs(int aqiOw) {
        switch (aqiOw) {
            case 1:  return 25;   // Bom
            case 2:  return 75;   // Razoável
            case 3:  return 125;  // Moderado
            case 4:  return 175;  // Ruim
            default: return 250;  // Muito Ruim
        }
    }

    private String getTitulo(int aqi) {
        String[] emojis = {"", "🟢", "🟡", "🟠", "🔴", "🟣"};
        String emoji = (aqi >= 1 && aqi <= 5) ? emojis[aqi] : "🔵";
        return emoji + " Qualidade do Ar: " + AirQualityUtils.statusAqi(aqi);
    }

    private void dispararNotificacaoSistema(String titulo, String mensagem) {
        NotificationManager manager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID, "Qualidade do Ar", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(canal);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), CANAL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(titulo)
                        .setContentText(mensagem)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}