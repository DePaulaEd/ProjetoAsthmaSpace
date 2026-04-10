package br.fmu.projetoasthmaspace.Data.Local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import br.fmu.projetoasthmaspace.Data.worker.NotificacaoDao;

@Database(entities = {NotificacaoEntity.class}, version = 1, exportSchema = false)
public abstract class NotificacaoDatabase extends RoomDatabase {

    public abstract NotificacaoDao dao();

    private static volatile NotificacaoDatabase INSTANCE;

    public static NotificacaoDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NotificacaoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NotificacaoDatabase.class,
                            "notificacoes_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
