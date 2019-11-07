package support.skipjack.adoi.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.matrix.androidsdk.data.Room;

@Entity
public class RoomEntry {
    @NonNull
    @PrimaryKey
    public  String id;
    public int tabType;

    public RoomEntry() {
    }

}
