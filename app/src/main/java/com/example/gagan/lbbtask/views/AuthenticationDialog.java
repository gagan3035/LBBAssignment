package com.example.gagan.lbbtask.views;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.gagan.lbbtask.constants.Constants;
import com.example.gagan.lbbtask.listeners.DialogActivityCallback;

/**
 * Created by Gagan on 5/28/2016.
 */
public class AuthenticationDialog extends Dialog {

    private static final String TAG = AuthenticationDialog.class.getSimpleName();
    private final DialogActivityCallback dialogActivityCallback;
    private WebView mWebView;
    private LinearLayout mContent;
    private ProgressDialog mSpinner;
    private Context mContext;
    private java.lang.String mUrl = "https://api.instagram.com/oauth/authorize/?client_id=" + Constants.CLIENT_ID + "&redirect_uri=" + Constants.REDIRECT_URI + "&response_type=code&display=touch&scope=public_content";

    double[] DIMENSIONS_LANDSCAPE;
    double[] DIMENSIONS_PORTRAIT;
    double height, width, land_height, land_width;

    public AuthenticationDialog(Context context, int h, int w) {
        super(context);
        mContext = context;
        dialogActivityCallback = (DialogActivityCallback)mContext;
        this.height = .82 * h;
        this.width = .87 * w;
        land_height = (.75) * h;
        land_width = (.75) * w;
        DIMENSIONS_PORTRAIT = new double[]{width, height};
        DIMENSIONS_LANDSCAPE = new double[]{land_width, land_height};
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpinner = new ProgressDialog(getContext());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading Please Wait...");
        mSpinner.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFBB5E60")));

        mContent = new LinearLayout(getContext());
        mContent.setOrientation(LinearLayout.VERTICAL);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setUpWebView();

        Display display = getWindow().getWindowManager().getDefaultDisplay();
        final float scale = getContext().getResources().getDisplayMetrics().density;

        double[] dimensions = (display.getWidth() < display.getHeight()) ? DIMENSIONS_PORTRAIT
                : DIMENSIONS_LANDSCAPE;


        addContentView(mContent, new FrameLayout.LayoutParams(
                (int) (dimensions[0]), (int) (dimensions[1])));

        CookieSyncManager.createInstance(getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    private void setUpWebView() {
        mWebView = new WebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new OAuthWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mContent.addView(mWebView);
    }

    private class OAuthWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Redirecting URL " + url);

            if (url.startsWith(Constants.REDIRECT_URI)) {
                String urls[] = url.split("=");
                if(dialogActivityCallback!=null)
                    dialogActivityCallback.onCodeReceived(urls[1]);

                return true;
            }


            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.d(TAG, "Page error: " + description);

            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "Loading URL: " + url);

            super.onPageStarted(view, url, favicon);
            if (!((Activity) mContext).isFinishing())
                mSpinner.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String title = mWebView.getTitle();

            Log.d(TAG, "onPageFinished URL: " + url);
            if (!((Activity) mContext).isFinishing())
                mSpinner.dismiss();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(dialogActivityCallback!=null)
            dialogActivityCallback.finishActivity();
    }

}
