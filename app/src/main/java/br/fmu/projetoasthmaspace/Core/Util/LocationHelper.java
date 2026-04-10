package br.fmu.projetoasthmaspace.Core.Util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {

    private static final double LAT_PADRAO = -23.5505;
    private static final double LON_PADRAO = -46.6333;

    public interface LocationCallback {
        void onLocation(double lat, double lon);
    }

    public static void obterLocalizacao(Context ctx, LocationCallback callback) {
        boolean temPermissao =
                ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED;

        if (!temPermissao) {
            callback.onLocation(LAT_PADRAO, LON_PADRAO);
            return;
        }

        LocationServices.getFusedLocationProviderClient(ctx)
                .getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        callback.onLocation(LAT_PADRAO, LON_PADRAO);
                    }
                })
                .addOnFailureListener(e -> callback.onLocation(LAT_PADRAO, LON_PADRAO));
    }
}