package support.skipjack.adoi.local_storage;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import support.skipjack.adoi.model.PageTokenEntity;
import support.skipjack.adoi.model.RoomEntry;
import support.skipjack.adoi.model.EventEntry;
import support.skipjack.adoi.local_storage.dao.EventDao;
import support.skipjack.adoi.local_storage.dao.RoomDao;
import support.skipjack.adoi.local_storage.dao.RoomTokenDao;

@Database(entities = {RoomEntry.class, EventEntry.class, PageTokenEntity.class},version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static AppDatabase GET;

    public abstract RoomDao roomDao();
    public abstract EventDao messageDao();
    public abstract RoomTokenDao roomTokenDao();
}