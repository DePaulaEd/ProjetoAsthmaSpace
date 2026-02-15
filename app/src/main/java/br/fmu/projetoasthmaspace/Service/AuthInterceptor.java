package br.fmu.projetoasthmaspace.Service;

import android.content.Context;

import java.io.IOException;

import br.fmu.projetoasthmaspace.Domain.UserSessionManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request original = chain.request();

        UserSessionManager session = new UserSessionManager(context);
        String token = session.getToken();

        if (token == null || token.isEmpty()) {
            return chain.proceed(original);
        }

        Request requestComToken = original.newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(requestComToken);
    }
}
