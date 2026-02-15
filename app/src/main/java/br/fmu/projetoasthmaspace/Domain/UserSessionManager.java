package br.fmu.projetoasthmaspace.Domain;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {

    private final SharedPreferences prefs;
    private static final String KEY_NOME = "USER_NAME";

    public UserSessionManager(Context context) {
        prefs = context.getSharedPreferences(
                SharedPreferencesKeys.PREFS_FILE_NAME,
                Context.MODE_PRIVATE
        );
    }

    /* TOKEN */
    public void saveToken(String token) {
        prefs.edit()
                .putString(SharedPreferencesKeys.TOKEN_KEY, token)
                .apply();
    }

    public String getToken() {
        return prefs.getString(SharedPreferencesKeys.TOKEN_KEY, null);
    }

    /* USER NAME */

    public void saveNome(String nome) {
        prefs.edit().putString(KEY_NOME, nome).apply();
    }

    public String getNome() {
        return prefs.getString(KEY_NOME, null);
    }

    /* CLEAR SESSION */
    public void clear() {
        prefs.edit().clear().apply();
    }
}
