package de.wegemann.klexikon;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SafeBrowsingResponse;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String HOME = "https://klexikon.zum.de/wiki/Hauptseite";
    private static final String HOST = "klexikon.zum.de";
    private static final String MEDIA_HOST = "upload.wikimedia.org";
    private WebView webView;
    private ProgressBar progress;
    private EditText search;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        buildInterface();
        configureWebView();
        if (state == null) webView.loadUrl(HOME); else webView.restoreState(state);
    }

    private void buildInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(6), dp(6), dp(6), dp(6));
        bar.setBackgroundColor(Color.rgb(62, 86, 104));

        Button back = makeButton("‹", "Zurück");
        Button home = makeButton("⌂", "Klexikon-Startseite");
        search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Was möchtest du wissen?");
        search.setTextSize(17);
        search.setBackgroundColor(Color.WHITE);
        search.setPadding(dp(12), 0, dp(12), 0);
        search.setImeActionLabel("Suchen", KeyEvent.KEYCODE_ENTER);
        Button go = makeButton("Suchen", "Im Klexikon suchen");

        bar.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        bar.addView(home, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(0, dp(48), 1f);
        searchParams.setMargins(dp(6), 0, dp(6), 0);
        bar.addView(search, searchParams);
        bar.addView(go, new LinearLayout.LayoutParams(dp(86), dp(48)));

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(217, 122, 74)));
        progress.setVisibility(View.GONE);

        webView = new WebView(this);
        root.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(60)));
        root.addView(progress, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3)));
        root.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);

        back.setOnClickListener(v -> goBack());
        home.setOnClickListener(v -> webView.loadUrl(HOME));
        go.setOnClickListener(v -> runSearch());
        search.setOnEditorActionListener((v, actionId, event) -> { runSearch(); return true; });
    }

    private Button makeButton(String text, String description) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(text.length() > 2 ? 14 : 27);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setContentDescription(description);
        button.setAllCaps(false);
        button.setPadding(dp(3), 0, dp(3), 0);
        return button;
    }

    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setSupportMultipleWindows(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setSafeBrowsingEnabled(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);

        webView.setDownloadListener((url, userAgent, disposition, mime, length) ->
                Toast.makeText(this, "Downloads sind in dieser App gesperrt.", Toast.LENGTH_SHORT).show());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int value) {
                progress.setProgress(value);
                progress.setVisibility(value < 100 ? View.VISIBLE : View.GONE);
            }
        });
        webView.setWebViewClient(new LockedClient());
    }

    private void runSearch() {
        String query = search.getText().toString().trim();
        if (query.isEmpty()) return;
        search.clearFocus();
        String url = "https://klexikon.zum.de/index.php?search=" + Uri.encode(query)
                + "&title=Spezial%3ASuche&go=Seite";
        webView.loadUrl(url);
    }

    private boolean isKlexikon(Uri uri) {
        return uri != null && "https".equalsIgnoreCase(uri.getScheme())
                && HOST.equalsIgnoreCase(uri.getHost());
    }

    private boolean isAllowedResource(Uri uri) {
        if (uri == null || !"https".equalsIgnoreCase(uri.getScheme())) return false;
        String host = uri.getHost();
        return HOST.equalsIgnoreCase(host) || MEDIA_HOST.equalsIgnoreCase(host);
    }

    private void goBack() {
        if (webView.canGoBack()) webView.goBack(); else webView.loadUrl(HOME);
    }

    @Override public void onBackPressed() { goBack(); }
    @Override protected void onSaveInstanceState(Bundle out) { webView.saveState(out); super.onSaveInstanceState(out); }
    @Override protected void onDestroy() { webView.destroy(); super.onDestroy(); }

    private class LockedClient extends WebViewClient {
        @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request.isForMainFrame() && !isKlexikon(request.getUrl())) {
                Toast.makeText(MainActivity.this,
                        "Dieser Link führt aus dem Klexikon heraus und wurde gesperrt.",
                        Toast.LENGTH_LONG).show();
                return true;
            }
            return false;
        }

        @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (!isAllowedResource(request.getUrl())) {
                return new WebResourceResponse("text/plain", "UTF-8", 403, "Blocked",
                        java.util.Collections.emptyMap(),
                        new ByteArrayInputStream(new byte[0]));
            }
            return null;
        }

        @Override public void onSafeBrowsingHit(WebView view, WebResourceRequest request,
                                                int threatType, SafeBrowsingResponse callback) {
            callback.backToSafety(true);
            Toast.makeText(MainActivity.this, "Unsichere Seite gesperrt.", Toast.LENGTH_LONG).show();
        }

        @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (request.isForMainFrame()) showOfflinePage();
        }
    }

    private void showOfflinePage() {
        String html = "<!doctype html><meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<style>body{font-family:sans-serif;padding:40px;color:#2c3d4c;text-align:center}"
                + "h1{font-size:28px}p{font-size:19px;line-height:1.5}</style>"
                + "<h1>Klexikon ist gerade nicht erreichbar</h1>"
                + "<p>Prüfe die Internetverbindung und tippe oben auf das Haus.</p>";
        webView.loadDataWithBaseURL(null, html, "text/html", StandardCharsets.UTF_8.name(), null);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
