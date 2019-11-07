package support.skipjack.adoi.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class PageTokenEntity {
    @NonNull
    @PrimaryKey
    public String roomId;

    public String startToken;
    public String endToken;

    public PageTokenEntity() {
    }

    @Ignore
    public PageTokenEntity(@NonNull String roomId, String startToken, String endToken) {
        this.startToken = startToken;
        this.roomId = roomId;
        this.endToken = endToken;
    }
}
