package com.skipjack.adoi.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.skipjack.adoi.R;
import com.skipjack.adoi._repository.RoomRepository;
import com.skipjack.adoi.base.Constants;
import com.skipjack.adoi.messaging.event.EventActivity;

import org.matrix.androidsdk.core.MXPatterns;
import org.matrix.androidsdk.data.Room;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import support.skipjack.adoi.matrix.MatrixCallback;
import support.skipjack.adoi.matrix.MatrixHelper;
import support.skipjack.adoi.matrix.MatrixService;

public class InviteUserDialog extends Dialog implements View.OnClickListener {

  public static InviteUserDialog dialogInstance;
  public EditText editText;
  public ProgressBar progressBar;
  public static Callback callback;
  private InviteUserDialog(Context context) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    View view =  LayoutInflater.from(getContext())
            .inflate(R.layout.layout_invite_user, null);

    editText  = view.findViewById(R.id.editText);
    progressBar  = view.findViewById(R.id.progressBarInvite);

   view.findViewById(R.id.btnInvite).setOnClickListener(this);
   view.findViewById(R.id.btnCancel).setOnClickListener(this);

    setContentView(view);

    int[]screenSize = getScreenSize();
    Window window = getWindow();
    window.setGravity(Gravity.BOTTOM);
    window.setLayout(screenSize[0],screenSize[1]);
    if (window != null) {
      window.setBackgroundDrawableResource(android.R.color.transparent);
    }
    setCancelable(false);
  }
  public static int[] getScreenSize(){

    return new int[]{ Resources.getSystem().getDisplayMetrics().widthPixels
            , Resources.getSystem().getDisplayMetrics().heightPixels};
  }
  public static void show(Context context,Callback c){
    callback = c;
    dialogInstance = new InviteUserDialog(context);
    dialogInstance.show();

  }

  @Override
  public void onClick(View v) {
    switch (v.getId()){
      case R.id.btnInvite:{
        progressBar.setVisibility(View.VISIBLE);
        String text = editText.getText().toString();
        if (text.replace(" ","").isEmpty()) {
          callback.onFailed("Empty search. Please type user to invite.");
          progressBar.setVisibility(View.GONE);
          return;
        }
        if (!text.substring(0,1).equals("@")){
          text = "@" + text;
        }

        if (!text.contains(":adoichat.com")){
          text = text+":adoichat.com";
        }
        List<Pattern> patterns = Arrays.asList(MXPatterns.PATTERN_CONTAIN_MATRIX_USER_IDENTIFIER, android.util.Patterns.EMAIL_ADDRESS);

        for (Pattern pattern : patterns) {
          Matcher matcher = pattern.matcher(text);
          while (matcher.find()) {
            try {
              String userId = text.substring(matcher.start(0), matcher.end(0));
              MatrixService.get().mxSession.createDirectMessageRoom(userId, new MatrixCallback<String>() {
                @Override
                public void onAPISuccess(String data) {
                  Room room = MatrixService.get().mxSession.getDataHandler()
                          .getStore().getRoom(data);
                  RoomRepository.insertRoomsToDB(room);
                  callback.onSuccess(room);
                  progressBar.setVisibility(View.GONE);
                  dismissNow();
                }

                @Override
                public void onAPIFailure(String errorMessage) {
                  callback.onFailed(errorMessage);
                  progressBar.setVisibility(View.GONE);
                    dismissNow();
                }
              });
            } catch (Exception e) {
              MatrixHelper.LOG("## displayInviteByUserId() " + e.getMessage());
              callback.onFailed("Failed to find user.");
              progressBar.setVisibility(View.GONE);
                dismissNow();
            }
          }
        }
      }break;
//      case R.id.btnBrowse:{selectedActionType = ActionType.BROWSE;}break;
      case R.id.btnCancel:{
        dismissNow();}break;
    }

  }

  private void dismissNow(){
      if (dialogInstance != null) {
          if (dialogInstance.isShowing()) {
              dialogInstance.dismiss();
              dialogInstance = null;
          }
      }
  }
  public interface Callback{
    void onSuccess(Room room);
    void onFailed(String message);
  }
}
