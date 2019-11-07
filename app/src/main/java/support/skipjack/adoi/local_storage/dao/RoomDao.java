package support.skipjack.adoi.local_storage.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import support.skipjack.adoi.model.RoomEntry;

/**
 * Dao for Item
 * */
@Dao
public interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(RoomEntry... roomEntries);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Completable update(RoomEntry... roomEntries);

    @Query("SELECT * FROM RoomEntry")
    Flowable<List<RoomEntry>> getAll();

    @Query("SELECT * FROM RoomEntry WHERE tabType LIKE :type")
    Flowable<List<RoomEntry>> getRoomsByType(int type);

    @Query("SELECT * FROM RoomEntry WHERE id LIKE :roomId")
    RoomEntry get(String roomId);

    @Query("DELETE FROM RoomEntry")
    void reset();
}
