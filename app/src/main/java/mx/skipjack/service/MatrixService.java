package mx.skipjack.service;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.skipjack.adoi.R;

import org.matrix.androidsdk.HomeServerConnectionConfig;
import org.matrix.androidsdk.MXDataHandler;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.core.model.MatrixError;
import org.matrix.androidsdk.crypto.IncomingRoomKeyRequest;
import org.matrix.androidsdk.crypto.IncomingRoomKeyRequestCancellation;
import org.matrix.androidsdk.crypto.MXCrypto;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomTag;
import org.matrix.androidsdk.data.metrics.MetricsListener;
import org.matrix.androidsdk.data.store.IMXStore;
import org.matrix.androidsdk.data.store.MXFileStore;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.client.LoginRestClient;
import org.matrix.androidsdk.rest.client.ProfileRestClient;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.rest.model.login.Credentials;
import org.matrix.androidsdk.ssl.Fingerprint;
import org.matrix.androidsdk.ssl.UnrecognizedCertificateException;

import java.util.Collection;
import java.util.Set;


public class MatrixService {

    private static final MatrixService ourInstance = new MatrixService();

    public static MatrixService get() {
        return ourInstance;
    }

    private MatrixService() {
    }

    private Context context;
    public HomeServerConnectionConfig homeServerConfig;
    public MXSession mxSession;
    public LoginRestClient loginRestClient;
    public ProfileRestClient profileRestClient;

    public Context getContext() {
        return context;
    }

    public void init(Context context) {
        this.context = context;
        homeServerConfig = new HomeServerConnectionConfig.Builder()
                .withHomeServerUri(Uri.parse(context.getString(R.string.HOST_SERVER)))
                .withIdentityServerUri(Uri.parse(context.getString(R.string.HOST_SERVER)))
                .build();

        loginRestClient = new LoginRestClient(homeServerConfig);
        profileRestClient = new ProfileRestClient(homeServerConfig);
    }





}
