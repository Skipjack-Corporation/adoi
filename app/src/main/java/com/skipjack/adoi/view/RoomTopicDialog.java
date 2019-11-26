package com.skipjack.adoi.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skipjack.adoi.R;

import org.matrix.androidsdk.data.Room;

import support.skipjack.adoi.matrix.MatrixCallback;

public class RoomTopicDialog extends Dialog implements View.OnClickListener {

  public static RoomTopicDialog dialogInstance;
  private static Room mRoom;
  public EditText editText;
  public ProgressBar progressBar;
  public static Callback callback;
  private RoomTopicDialog(Context context) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    View view =  LayoutInflater.from(getContext())
            .inflate(R.layout.layout_room_name, null);
    TextView textView = view.findViewById(R.id.textHeaderTitle);
    editText  = view.findViewById(R.id.editText);
    progressBar  = view.findViewById(R.id.progressBarInvite);

    textView.setText("Topic");
    editText.setText(mRoom.getTopic());
   view.findViewById(R.id.btnOk).setOnClickListener(this);
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
  public static void show(Context context, Room room,Callback c){
    mRoom = room;
    callback = c;
    dialogInstance = new RoomTopicDialog(context);
    dialogInstance.show();

  }

  @Override
  public void onClick(View v) {
    switch (v.getId()){
      case R.id.btnOk:{
        progressBar.setVisibility(View.VISIBLE);
        String text = editText.getText().toString();
        if (text.replace(" ","").isEmpty()) {
          callback.onFailed("Cannot save empty name.");
          progressBar.setVisibility(View.GONE);
          return;

        }else if (text.equals(mRoom.getRoomDisplayName(getContext()))){
          progressBar.setVisibility(View.GONE);
          dismissNow();
          return;
        }

        mRoom.updateTopic(text, new MatrixCallback<Void>() {
          @Override
          public void onAPISuccess(Void data) {
            callback.onSuccess(text);
            progressBar.setVisibility(View.GONE);
            dismissNow();
          }

          @Override
          public void onAPIFailure(String errorMessage) {
            callback.onFailed(errorMessage);
            progressBar.setVisibility(View.GONE);
          }
        });




      }break;
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
    void onSuccess(String name);
    void onFailed(String message);
  }
}
