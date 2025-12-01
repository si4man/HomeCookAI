package com.lvl.homecookai;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HistoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        view.findViewById(R.id.view_button_1).setOnClickListener(v -> openRecipeDetail(
            "Pasta Carbonara",
            "- Pasta\n- Eggs\n- Bacon\n- Parmesan Cheese",
            "20 min",
            "1. Boil pasta in salted water\n2. Fry bacon until crispy\n3. Mix eggs with grated cheese\n4. Combine hot pasta with bacon\n5. Add egg mixture and toss quickly\n6. Serve immediately with black pepper"
        ));

        view.findViewById(R.id.view_button_2).setOnClickListener(v -> openRecipeDetail(
            "Pizza Margarita",
            "- Pizza Dough\n- Tomato Sauce\n- Mozzarella\n- Fresh Basil\n- Olive Oil",
            "30 min",
            "1. Preheat oven to 220°C\n2. Stretch pizza dough\n3. Spread tomato sauce evenly\n4. Add mozzarella cheese\n5. Bake for 12-15 minutes\n6. Add fresh basil and olive oil before serving"
        ));

        view.findViewById(R.id.view_button_3).setOnClickListener(v -> openRecipeDetail(
            "Caesar Salad",
            "- Romaine Lettuce\n- Croutons\n- Parmesan Cheese\n- Caesar Dressing\n- Black Pepper\n- Lemon",
            "15 min",
            "1. Wash and chop romaine lettuce\n2. Toss with caesar dressing\n3. Add croutons\n4. Sprinkle parmesan cheese\n5. Add black pepper\n6. Squeeze fresh lemon juice\n7. Serve immediately"
        ));
    }

    private void openRecipeDetail(String recipeName, String ingredients, String time, String instructions) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_NAME, recipeName);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_INGREDIENTS, ingredients);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_TIME, time);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_INSTRUCTIONS, instructions);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_IMAGE, R.drawable.ic_launcher_foreground);
        startActivity(intent);
    }
}
