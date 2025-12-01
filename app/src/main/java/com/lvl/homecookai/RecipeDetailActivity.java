package com.lvl.homecookai;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_NAME = "recipe_name";
    public static final String EXTRA_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String EXTRA_RECIPE_TIME = "recipe_time";
    public static final String EXTRA_RECIPE_INSTRUCTIONS = "recipe_instructions";
    public static final String EXTRA_RECIPE_IMAGE = "recipe_image";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        String recipeName = getIntent().getStringExtra(EXTRA_RECIPE_NAME);
        String ingredients = getIntent().getStringExtra(EXTRA_RECIPE_INGREDIENTS);
        String time = getIntent().getStringExtra(EXTRA_RECIPE_TIME);
        String instructions = getIntent().getStringExtra(EXTRA_RECIPE_INSTRUCTIONS);
        int imageResId = getIntent().getIntExtra(EXTRA_RECIPE_IMAGE, R.drawable.ic_launcher_foreground);

        ImageView recipeImage = findViewById(R.id.recipe_image);
        TextView recipeTitleText = findViewById(R.id.recipe_title);
        TextView ingredientsText = findViewById(R.id.ingredients_text);
        TextView timeText = findViewById(R.id.time_text);
        TextView instructionsText = findViewById(R.id.instructions_text);

        recipeImage.setImageResource(imageResId);
        recipeTitleText.setText(recipeName);
        ingredientsText.setText(ingredients);
        timeText.setText(time);
        instructionsText.setText(instructions);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(recipeName);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
