package com.lvl.homecookai;

import android.content.Context;
import android.content.SharedPreferences;

public class PromptManager {
    private static final String PREFS_NAME = "prompt_prefs";
    private static final String PROMPT_KEY = "gemini_prompt";
    
    private final SharedPreferences sharedPreferences;

    public PromptManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getDefaultPrompt() {
        return sharedPreferences.getString(PROMPT_KEY,
                "Analyze this image and identify ALL the ingredients you can see. " +
                "Return ONLY a valid JSON object (no markdown, no extra text) with this exact structure:\n" +
                "{\n" +
                "  \"ingredients\": [\"ingredient1\", \"ingredient2\", \"ingredient3\", ...]\n" +
                "}\n" +
                "List each ingredient once, use common names (e.g., 'tomato' instead of 'ripe red tomato'). " +
                "If no ingredients found, return: {\"ingredients\": []}");
    }

    public void savePrompt(String prompt) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PROMPT_KEY, prompt);
        editor.apply();
    }

    public void resetToDefault() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PROMPT_KEY);
        editor.apply();
    }
}
