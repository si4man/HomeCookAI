package com.lvl.homecookai;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executors;

public class GeminiService {

    public ListenableFuture<String> generateContent(String textPrompt) {
        GenerativeModel gm = new GenerativeModel(
                "gemini-2.5-flash",
                BuildConfig.GEMINI_API_KEY
        );
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText(textPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        return Futures.transform(response, it -> {
            if (it == null) {
                return "Error: Empty response.";
            }
            String text = it.getText();
            if (text == null) {
                return "Error: No text found in response.";
            }
            return text;
        }, Executors.newSingleThreadExecutor());
    }
}
