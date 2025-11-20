package br.fmu.projetoasthmaspace.Domain;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import br.fmu.projetoasthmaspace.Login;

public class LogoutUtil {

    public static void logout(Context context) {

        SharedPreferences prefs = context.getSharedPreferences("APP", Context.MODE_PRIVATE);
        prefs.edit().remove("TOKEN").apply();

        Intent intent = new Intent(context, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}

