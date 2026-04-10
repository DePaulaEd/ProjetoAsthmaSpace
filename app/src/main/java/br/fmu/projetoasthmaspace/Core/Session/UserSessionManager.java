package br.fmu.projetoasthmaspace.Core.Session;

import android.content.Context;
import android.content.SharedPreferences;

import br.fmu.projetoasthmaspace.Core.Domain.Log.SharedPreferencesKeys;

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
    public Long getClienteId() {
        return prefs.getLong("CLIENTE_ID", -1L);
    }

    public String getToken() {
        return prefs.getString(SharedPreferencesKeys.TOKEN_KEY, null);
    }

    public void saveClienteId(Long id) {
        prefs.edit().putLong("CLIENTE_ID", id).apply();
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
