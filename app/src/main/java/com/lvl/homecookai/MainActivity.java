package com.lvl.homecookai;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView geminiResponseTextView = findViewById(R.id.gemini_response);

        GeminiService geminiService = new GeminiService();
        ListenableFuture<String> result = geminiService.generateContent("Напиши рецепт борща в 3 предложениях");
        Futures.addCallback(result, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    geminiResponseTextView.setText(result);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    geminiResponseTextView.setText("Error: " + t.getMessage());
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }
}
