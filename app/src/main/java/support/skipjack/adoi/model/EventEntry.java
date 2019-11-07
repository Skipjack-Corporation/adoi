package support.skipjack.adoi.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class EventEntry{
    @NonNull
    @PrimaryKey
    public String eventId;
    public String roomId;

    public EventEntry() {
    }

}
