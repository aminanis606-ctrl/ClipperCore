package com.clipper.core;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("Clipper Core");
        title.setTextSize(24);

        EditText url = new EditText(this);
        url.setHint("YouTube Podcast URL");

        Button button = new Button(this);
        button.setText("Cari Clip");

        TextView status = new TextView(this);
        status.setText("Siap.");

        button.setOnClickListener(v -> {
            String value = url.getText().toString().trim();

            if (value.isEmpty()) {
                status.setText("Masukkan URL YouTube.");
            } else {
                status.setText("URL diterima. Pipeline akan disambungkan.");
            }
        });

        layout.addView(title);
        layout.addView(url);
        layout.addView(button);
        layout.addView(status);

        setContentView(layout);
    }
}
