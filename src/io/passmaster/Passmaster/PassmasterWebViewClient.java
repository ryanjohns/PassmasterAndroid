package io.passmaster.Passmaster;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PassmasterWebViewClient extends WebViewClient {

  private final String passmasterErrorHTML =
      "<html>" +
      "<head>" +
        "<style type='text/css'>" +
          "body { background-color: #8b99ab; color: #fff; text-align: center; font-family: arial, sans-serif; }" +
        "</style>" +
        "<script type='text/javascript'>" +
          "function MobileApp() {};" +
          "MobileApp.updateAppCache = function() {" +
            PassmasterJsInterface.JS_NAMESPACE + ".loadPassmaster();" +
          "};" +
        "</script>" +
      "</head>" +
      "<body>" +
        "<div>" +
          "<h2>Passmaster</h2>" +
          "<h4>We're sorry, but something went wrong.</h4>" +
          "<h4>%s</h4>" +
        "</div>" +
      "</body>" +
      "</html>";

  @Override
  public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    String errorString = String.format(passmasterErrorHTML, description);
    view.loadData(errorString, "text/html", null);
  }

}
