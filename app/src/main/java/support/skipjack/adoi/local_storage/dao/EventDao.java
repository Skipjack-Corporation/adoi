package support.skipjack.adoi.local_storage.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import support.skipjack.adoi.model.EventEntry;

/**
 * Dao for Item
 * */
@Dao
public interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable add(EventEntry... messageEntries);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Completable update(EventEntry... messageEntries);

    @Query("SELECT * FROM EventEntry")
    Flowable<List<EventEntry>> getAll();

//    @Query("SELECT * FROM EventEntry WHERE roomId  LIKE :roomId AND eventType LIKE :eventType  ORDER BY timeStamp DESC")
//    Flowable<List<EventEntry>>  getList(String roomId, String eventType);

    @Query("SELECT * FROM EventEntry WHERE roomId  LIKE :roomId")
    Flowable<List<EventEntry>>  getList(String roomId);

    @Query("DELETE FROM EventEntry")
    void reset();

    @Query("SELECT COUNT(roomId) FROM EventEntry WHERE roomId  LIKE :roomId")
    Flowable<Integer> getRowCount(String roomId);
}
