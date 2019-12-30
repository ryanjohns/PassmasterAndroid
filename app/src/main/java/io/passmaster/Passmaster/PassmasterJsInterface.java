package io.passmaster.Passmaster;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.security.KeyStore;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.FragmentActivity;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public final class PassmasterJsInterface {

  static final String JS_NAMESPACE = "AndroidJs";
  private WebView webView;
  private final WeakReference<Activity> activityRef;
  private long lockTime;

  private String masterPasswordHash;
  private String userId;

  private boolean promptForEncryption;
  private boolean retryPrompt;

  PassmasterJsInterface(Activity activity, WebView webView) {
    activityRef = new WeakReference<>(activity);
    this.webView = webView;
    lockTime = 0;
  }

  @JavascriptInterface
  public void loadPassmaster() {
    final Activity activity = activityRef.get();
    activity.runOnUiThread(() -> webView.loadUrl(PassmasterActivity.PASSMASTER_URL));
  }

  @JavascriptInterface
  public void copyToClipboard(String text) {
    final Activity activity = activityRef.get();
    ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("Passmaster Data", text);
    if (clipboard != null) {
      clipboard.setPrimaryClip(clip);
    }
  }

  @JavascriptInterface
  public void checkLockTime() {
    if (lockTime > 0 && lockTime < System.currentTimeMillis() / 1000) {
      final Activity activity = activityRef.get();
      activity.runOnUiThread(() -> webView.loadUrl("javascript:MobileApp.lock();"));
    }
  }

  @JavascriptInterface
  public void saveLockTime(int minutes) {
    if (minutes == 0) {
      lockTime = 0;
    } else {
      lockTime = (System.currentTimeMillis() / 1000) + (minutes * 60);
    }
  }

  @JavascriptInterface
  @RequiresApi(api = Build.VERSION_CODES.M)
  public void savePasswordForTouchID(String userId, String password, String enabled){
    if (!touchIDSupported()) {
      return;
    }

    if (!enabled.equals("true")) {
      return;
    }

    this.masterPasswordHash = password;
    this.userId = userId;
    this.retryPrompt = true;

    generateSecretKey(this.userId);
    encryptPassword();
  }

  @JavascriptInterface
  public void deletePasswordForTouchID(String userId){
    deleteSecretKey(userId);
    deleteUserPref(userId, "initialization vector");
    deleteUserPref(userId, "encrypted_password");
  }

  @JavascriptInterface
  public void checkForTouchIDUsability(String userId, String enabled) {
    boolean isSupported = touchIDSupported();
    boolean hasPassword = getUserPref(userId, "encrypted_password") != null;
    boolean userEnabled = enabled.equals("true");
    boolean faceIDSupported = false;

    if (isSupported && hasPassword && !userEnabled) {
      deletePasswordForTouchID(userId);
      hasPassword = false;
    }

    String params = isSupported + ", " + (hasPassword && isSupported) + ", " + faceIDSupported;
    activityRef.get().runOnUiThread(() -> webView.loadUrl("javascript:MobileApp.setTouchIDUsability("+ params +");"));
  }

  @JavascriptInterface
  @RequiresApi(api = Build.VERSION_CODES.M)
  public void authenticateWithTouchID(String userId) {
    this.promptForEncryption = false;
    this.retryPrompt = true;
    this.userId = userId;
    biometricAuthenticate();
    activityRef.get().runOnUiThread(() -> webView.loadUrl("javascript:MobileApp.userFallbackForTouchID();"));
  }

  private boolean touchIDSupported()
  {
    Context context = activityRef.get().getApplicationContext();
    return (BiometricManager.from(context).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS);
  }

  private void deleteSecretKey(String keyAlias) {
    try {
      KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
      keyStore.load(null);

      if (keyStore.containsAlias(keyAlias)) {
        keyStore.deleteEntry((keyAlias));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void generateSecretKey(String keyAlias) {
    try {
      KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
      keyStore.load(null);
      if (!keyStore.containsAlias(keyAlias)) {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        keyGenerator.init(new KeyGenParameterSpec.Builder(keyAlias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(60)
                .build());
        keyGenerator.generateKey();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Cipher getCipher() {
    try {
      return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
              + KeyProperties.BLOCK_MODE_CBC + "/"
              + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private SecretKey getSecretKey(String keyAlias) {
    try {
      KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
      keyStore.load(null);
      if (keyStore.containsAlias(keyAlias)) {
        return ((SecretKey) keyStore.getKey(keyAlias, null));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void encryptPassword() {
    Cipher cipher = getCipher();
    SecretKey secretKey = getSecretKey(userId);
    try {
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      String IV = Base64.encodeToString(cipher.getIV(), Base64.URL_SAFE);
      byte[] encryptedBytes = cipher.doFinal(masterPasswordHash.getBytes(Charset.defaultCharset()));
      String encryptedString = Base64.encodeToString(encryptedBytes, Base64.URL_SAFE);
      saveUserPref(userId,"initialization vector", IV);
      saveUserPref(userId,"encrypted_password", encryptedString);
    } catch (Exception e) {
      if (e instanceof UserNotAuthenticatedException) {
        if (retryPrompt) {
          retryPrompt = false;
          promptForEncryption = true;
          biometricAuthenticate();
        }
      } else {
        e.printStackTrace();
      }
    }
  }

  private void decryptPassword() {
    Cipher cipher = getCipher();
    SecretKey secretKey = getSecretKey(userId);
    try {
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(Base64.decode(getUserPref(userId,"initialization vector"), Base64.URL_SAFE)));
      byte[] decryptedBytes = cipher.doFinal(Base64.decode(getUserPref(userId,"encrypted_password"), Base64.URL_SAFE));
      String decryptedString = new String(decryptedBytes, "UTF-8");
      activityRef.get().runOnUiThread(() -> webView.loadUrl("javascript:MobileApp.unlockWithPasswordFromTouchID('" + decryptedString + "');"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  @RequiresApi(api = Build.VERSION_CODES.M)
  public void biometricAuthenticate() {
    BiometricPrompt.PromptInfo promptInfo = buildBiometricPromptInfo();
    activityRef.get().runOnUiThread(() -> buildBiometricPrompt().authenticate(promptInfo));
  }

  private BiometricPrompt.PromptInfo buildBiometricPromptInfo() {
    return new BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock your Passmaster account")
            .setNegativeButtonText("Cancel")
            .build();
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private BiometricPrompt buildBiometricPrompt() {
    return new BiometricPrompt((FragmentActivity) activityRef.get(),
            Executors.newSingleThreadExecutor(),
            biometricCallback);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private BiometricPrompt.AuthenticationCallback biometricCallback = new BiometricPrompt.AuthenticationCallback() {
    @Override
    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
      super.onAuthenticationSucceeded(result);
      try {
        if (promptForEncryption) {
          encryptPassword();
        } else {
          decryptPassword();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };

  private void saveUserPref(String userId, String key, String value) {
    SharedPreferences sharedPref = activityRef.get().getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putString(key + userId, value);
    editor.commit();
  }

  private String getUserPref(String userId, String key) {
    SharedPreferences sharedPref = activityRef.get().getPreferences(Context.MODE_PRIVATE);
    String value = sharedPref.getString(key + userId, null);
    return value;
  }

  private void deleteUserPref(String userId, String key) {
    SharedPreferences sharedPref = activityRef.get().getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.remove(key + userId);
    editor.commit();
  }
}
