package io.passmaster.Passmaster;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;

public class PassmasterWebChromeClient extends WebChromeClient {

  private final WeakReference<Activity> activityRef;

  public PassmasterWebChromeClient(Activity activity) {
    activityRef = new WeakReference<Activity>(activity);
  }

  @Override
  public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
    final Activity activity = activityRef.get();
    AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK);
    builder.setTitle("Passmaster");
    builder.setMessage(message);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        result.confirm();
      }
    });
    builder.create();
    builder.show();
    return true;
  }

  @Override
  public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
    final Activity activity = activityRef.get();
    AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK);
    builder.setTitle("Passmaster");
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

  @Override
  public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
    final Activity activity = activityRef.get();
    AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK);
    builder.setTitle("Passmaster");
    builder.setMessage(message);
    final EditText input = new EditText(activity);
    input.setText(defaultValue);
    builder.setView(input);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        result.confirm(input.getText().toString());
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
