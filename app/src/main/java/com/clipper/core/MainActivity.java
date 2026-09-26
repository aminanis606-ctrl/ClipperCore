package com.clipper.core;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

public class MainActivity extends Activity {

    private void runPrefilter(
            String transcript,
            String sourceUrl,
            TextView status
    ) {
        status.setText("Menjalankan PREFILTER...");

        new Thread(() -> {
            try {
                Python python = Python.getInstance();
                PyObject module = python.getModule("prefilter");

                PyObject candidates =
                        module.callAttr("find_candidates", transcript);

                PyObject prompt =
                        module.callAttr(
                                "build_gemini_prompt",
                                candidates,
                                sourceUrl
                        );

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    throw new IllegalStateException(
                            "Output Download memerlukan Android 10+."
                    );
                }

                String relativePath =
                        Environment.DIRECTORY_DOWNLOADS + "/ClipperCore/";

                getContentResolver().delete(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        MediaStore.MediaColumns.DISPLAY_NAME + "=? AND "
                                + MediaStore.MediaColumns.RELATIVE_PATH + "=?",
                        new String[]{"prompt.txt", relativePath}
                );

                ContentValues values = new ContentValues();
                values.put(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        "prompt.txt"
                );
                values.put(
                        MediaStore.MediaColumns.MIME_TYPE,
                        "text/plain"
                );
                values.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        relativePath
                );
                values.put(
                        MediaStore.MediaColumns.IS_PENDING,
                        1
                );

                Uri outputUri = getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        values
                );

                if (outputUri == null) {
                    throw new IllegalStateException(
                            "Gagal membuat prompt.txt di Download."
                    );
                }

                try (OutputStream out =
                             getContentResolver().openOutputStream(outputUri)) {
                    if (out == null) {
                        throw new IllegalStateException(
                                "Gagal membuka prompt.txt."
                        );
                    }

                    out.write(
                            prompt.toJava(String.class)
                                    .getBytes(StandardCharsets.UTF_8)
                    );
                }

                ContentValues ready = new ContentValues();
                ready.put(MediaStore.MediaColumns.IS_PENDING, 0);
                getContentResolver().update(
                        outputUri,
                        ready,
                        null,
                        null
                );

                int count = candidates.asList().size();

                runOnUiThread(() ->
                        status.setText(
                                "PREFILTER selesai: " + count
                                        + " kandidat.\n"
                                        + "Prompt: Download/ClipperCore/prompt.txt"
                        )
                );
            } catch (Exception e) {
                runOnUiThread(() ->
                        status.setText(
                                "PREFILTER gagal: " + e.getMessage()
                        )
                );
            }
        }).start();
    }

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
            String value = url.getText().toString().trim();

            if (!srt.isEmpty()) {
                runPrefilter(srt, value, status);
                return;
            }

            if (value.isEmpty()) {
                status.setText("Masukkan URL YouTube atau transcript SRT.");
                return;
            }

            status.setText("Mengambil transcript YouTube...");

            new Thread(() -> {
                try {
                    Python python = Python.getInstance();
                    PyObject module = python.getModule("main");
                    String result = module.callAttr("transcript_srt", value)
                            .toJava(String.class);

                    runOnUiThread(() -> {
                        transcript.setText(result);
                        runPrefilter(result, value, status);
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            status.setText(
                                    "Transcript gagal diambil: " + e.getMessage()
                            )
                    );
                }
            }).start();
        });

        layout.addView(title);
        layout.addView(url);
        layout.addView(transcript);
        layout.addView(button);
        layout.addView(status);

        setContentView(layout);
    }
}
