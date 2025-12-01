package com.lvl.homecookai;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int PERMISSION_CAMERA = 100;
    private static final int PERMISSION_READ_STORAGE = 101;

    private TextInputEditText inputRecipe;
    private MaterialCardView cameraCard;
    private String currentPhotoPath;
    private GeminiService geminiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputRecipe = view.findViewById(R.id.inputRecipe);
        cameraCard = view.findViewById(R.id.camera_card);
        geminiService = new GeminiService();

        cameraCard.setOnClickListener(v -> showCameraMenu(v));
    }

    private void showCameraMenu(View view) {
        PopupMenu menu = new PopupMenu(requireContext(), view);
        menu.getMenuInflater().inflate(R.menu.camera_choice_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_take_photo) {
                openCamera();
                return true;
            } else if (item.getItemId() == R.id.menu_gallery) {
                openGallery();
                return true;
            }
            return false;
        });
        menu.show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(requireContext(),
                        "com.lvl.homecookai.provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_GALLERY);
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_READ_STORAGE);
                return;
            }
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_GALLERY);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                handleCameraImage();
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                handleGalleryImage(data.getData());
            }
        }
    }

    private void handleCameraImage() {
        if (currentPhotoPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            if (bitmap != null) {
                analyzeImage(bitmap);
            }
        }
    }

    private void handleGalleryImage(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            if (bitmap != null) {
                analyzeImage(bitmap);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error loading image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void analyzeImage(Bitmap bitmap) {
        PromptManager promptManager = new PromptManager(requireContext());
        String prompt = promptManager.getDefaultPrompt();

        Toast.makeText(requireContext(), "Analyzing image...", Toast.LENGTH_SHORT).show();

        Futures.addCallback(
                geminiService.generateContentWithImage(bitmap, prompt),
                new FutureCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        if (isAdded()) {
                            String ingredients = extractIngredients(result);
                            inputRecipe.setText(ingredients);
                            Toast.makeText(requireContext(), "Ingredients extracted!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                ContextCompat.getMainExecutor(requireContext())
        );
    }

    private String extractIngredients(String result) {
        try {
            String jsonString = result.trim();
            
            if (jsonString.startsWith("```json")) {
                jsonString = jsonString.substring(7);
            }
            if (jsonString.startsWith("```")) {
                jsonString = jsonString.substring(3);
            }
            if (jsonString.endsWith("```")) {
                jsonString = jsonString.substring(0, jsonString.length() - 3);
            }
            jsonString = jsonString.trim();

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray ingredientsArray = jsonObject.getJSONArray("ingredients");

            StringBuilder ingredientsStr = new StringBuilder();
            for (int i = 0; i < ingredientsArray.length(); i++) {
                if (i > 0) {
                    ingredientsStr.append(", ");
                }
                ingredientsStr.append(ingredientsArray.getString(i));
            }

            return ingredientsStr.toString();
        } catch (JSONException e) {
            Toast.makeText(requireContext(), "Could not parse JSON response, trying fallback",
                    Toast.LENGTH_SHORT).show();
            return extractIngredientsManual(result);
        }
    }

    private String extractIngredientsManual(String result) {
        StringBuilder ingredients = new StringBuilder();
        try {
            int startIdx = 0;
            while (true) {
                int quoteStart = result.indexOf("\"", startIdx);
                if (quoteStart == -1) break;
                
                int quoteEnd = result.indexOf("\"", quoteStart + 1);
                if (quoteEnd == -1) break;
                
                String ingredient = result.substring(quoteStart + 1, quoteEnd);
                
                if (!ingredient.equals("ingredients")) {
                    if (ingredients.length() > 0) {
                        ingredients.append(", ");
                    }
                    ingredients.append(ingredient);
                }
                
                startIdx = quoteEnd + 1;
            }
            
            if (ingredients.length() > 0) {
                return ingredients.toString();
            }
        } catch (Exception e) {
        }
        
        return result.substring(0, Math.min(300, result.length()));
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error creating file: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_READ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
