package br.fmu.projetoasthmaspace.Domain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class LembreteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String titulo = intent.getStringExtra("titulo");
        String mensagem = intent.getStringExtra("mensagem");

        // Exemplo: apenas alerta Toast, você pode trocar por Notification
        Toast.makeText(context, titulo + ": " + mensagem, Toast.LENGTH_LONG).show();
    }
}


