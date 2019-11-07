package support.skipjack.adoi.local_storage.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import io.reactivex.Completable;
import io.reactivex.Single;
import support.skipjack.adoi.model.PageTokenEntity;

/**
 * Dao for Item
 * */
@Dao
public interface RoomTokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable add(PageTokenEntity... pageTokenEntities);

    @Query("SELECT * FROM PageTokenEntity WHERE roomId LIKE :roomId")
    Single<PageTokenEntity> get(String roomId);

    @Query("DELETE FROM PageTokenEntity WHERE roomId LIKE :roomId")
    void delete(String roomId);
}
