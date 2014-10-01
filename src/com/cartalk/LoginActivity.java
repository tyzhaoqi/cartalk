package com.cartalk;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Window;
import android.widget.TextView;

public class LoginActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);
        TextView registview = (TextView)this.findViewById(R.id.register_link);
        //registview.setAutoLinkMask(Linkify.ALL);
        registview.setText(Html.fromHtml("<a href='http://cartalk.sinaapp.com/account/'>зЂВс</a>"));
        registview.setMovementMethod(LinkMovementMethod.getInstance());
    }
}