package support.skipjack.adoi.local_storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.matrix.androidsdk.rest.model.login.Credentials;

public class AppSharedPreference {
    private static final AppSharedPreference ourInstance = new AppSharedPreference();

    public static AppSharedPreference get() {
        return ourInstance;
    }

    private AppSharedPreference() {
    }

    private static final String NAME = "Adoi_shared_pref_v1";
    private static final String KEY_CREDENTIAL = "loginCredentials";
    private static final String KEY_MESSAGING_BADGECOUNT = "messagingCount";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public void  init(Context context){
        sharedPreferences = context.getSharedPreferences(NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveLoginCredential(Credentials credentials){
        String str = new Gson().toJson(credentials,Credentials.class);
        editor.putString(KEY_CREDENTIAL,str);
        editor.commit();
    }

    public Credentials getLoginCredential(){
        String data = sharedPreferences.getString(KEY_CREDENTIAL,null);

        if (data != null)
            return new Gson().fromJson(data, Credentials.class);

        return null;
    }

    public boolean hasLoginCredentials(){
        return  getLoginCredential() != null;
    }

    public void setMessagingCount(int count){
        editor.putInt(KEY_MESSAGING_BADGECOUNT,count);
        editor.commit();
    }
    public int getMessaingCount(){
        return sharedPreferences.getInt(KEY_MESSAGING_BADGECOUNT,0);
    }
}
