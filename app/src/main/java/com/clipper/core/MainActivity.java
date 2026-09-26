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

        EditText transcript = new EditText(this);
        transcript.setHint("Tempel transcript SRT");
        transcript.setGravity(android.view.Gravity.TOP);
        transcript.setMinLines(8);
        transcript.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        Button button = new Button(this);
        button.setText("Analisis Transcript");

        TextView status = new TextView(this);
        status.setText("Siap.");

        button.setOnClickListener(v -> {
            String srt = transcript.getText().toString().trim();

            if (srt.isEmpty()) {
                status.setText("Masukkan transcript SRT.");
            } else {
                status.setText("Transcript SRT diterima. Siap masuk PREFILTER.");
            }
        });

        layout.addView(title);
        layout.addView(url);
        layout.addView(transcript);
        layout.addView(button);
        layout.addView(status);

        setContentView(layout);
    }
}
