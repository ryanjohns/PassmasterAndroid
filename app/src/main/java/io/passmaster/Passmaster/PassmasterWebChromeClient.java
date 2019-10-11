package io.passmaster.Passmaster;

import android.app.Activity;
import android.app.AlertDialog;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;

import java.lang.ref.WeakReference;

public class PassmasterWebChromeClient extends WebChromeClient {

  private final WeakReference<Activity> activityRef;

  PassmasterWebChromeClient(Activity activity) {
    activityRef = new WeakReference<>(activity);
  }

  @Override
  public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
    final Activity activity = activityRef.get();
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setTitle("Passmaster");
    builder.setMessage(message);
    builder.setPositiveButton("OK", (dialog, id) -> result.confirm());
    builder.create();
    builder.show();
    return true;
  }

  @Override
  public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
    final Activity activity = activityRef.get();
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setTitle("Passmaster");
    builder.setMessage(message);
    builder.setPositiveButton("OK", (dialog, id) -> result.confirm());
    builder.setNegativeButton("Cancel", (dialog, id) -> result.cancel());
    builder.create();
    builder.show();
    return true;
  }

  @Override
  public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
    final Activity activity = activityRef.get();
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setTitle("Passmaster");
    builder.setMessage(message);
    final EditText input = new EditText(activity);
    input.setText(defaultValue);
    builder.setView(input);
    builder.setPositiveButton("OK", (dialog, id) -> result.confirm(input.getText().toString()));
    builder.setNegativeButton("Cancel", (dialog, id) -> result.cancel());
    builder.create();
    builder.show();
    return true;
  }

}
