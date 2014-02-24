package io.passmaster.Passmaster;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

public class PassmasterWebChromeClient extends WebChromeClient {

  private final WeakReference<Activity> activityRef;

  public PassmasterWebChromeClient(Activity activity) {
    activityRef = new WeakReference<Activity>(activity);
  }

  @Override
  public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
    result.confirm();
    final Activity activity = activityRef.get();
    Toast toast = Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_LONG);
    toast.setGravity(Gravity.CENTER, 0, 0);
    toast.show();
    return true;
  }

  @Override
  public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
    final Activity activity = activityRef.get();
    AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK);
    builder.setTitle("Log Out");
    builder.setMessage(message);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        result.confirm();
      }
    });
    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        result.cancel();
      }
    });
    builder.create();
    builder.show();
    return true;
  }

}
