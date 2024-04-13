package com.example.recipefinder.models;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.ImageView;

import com.example.recipefinder.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.List;

public class Recipe {
    private int id;
    private String title;
    private String image;
    private Drawable imageDrawable; // Updated
    private List<Ingredient> missedIngredients;
    private List<Ingredient> usedIngredients;
    private String instructions;

    public Recipe(int id, String title, String image, List<Ingredient> missedIngredients, List<Ingredient> usedIngredients, String instructions) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.missedIngredients = missedIngredients;
        this.usedIngredients = usedIngredients;
        this.instructions = instructions;
    }

    public Recipe(String title, String image, List<Ingredient> usedIngredients, String instructions) {
        this.title = title;
        this.image = image;
        this.usedIngredients = usedIngredients;
        this.instructions = instructions;
    }

    public Recipe(String title){
        this.title=title;
    }

    public Recipe(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Ingredient> getMissedIngredients() {
        return missedIngredients;
    }

    public void setMissedIngredients(List<Ingredient> missedIngredients) {
        this.missedIngredients = missedIngredients;
    }

    public List<Ingredient> getUsedIngredients() {
        return usedIngredients;
    }

    public void setUsedIngredients(List<Ingredient> usedIngredients) {
        this.usedIngredients = usedIngredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getMissingIngredientsString(List<Ingredient> missedIngredients){
        StringBuilder stringBuilder = new StringBuilder();
        for (Ingredient ingredient : missedIngredients) {
            stringBuilder.append(ingredient.getName()).append(",");
        }
        // Remove the last comma
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    public String getUsedIngredientsString(List<Ingredient> missedIngredients){
        StringBuilder stringBuilder = new StringBuilder();
        for (Ingredient ingredient : missedIngredients) {
            stringBuilder.append(ingredient.getName()).append(",");
        }
        // Remove the last comma
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

//    public void loadImageIntoImageView(ImageView imageView) {
//        if (image != null && !image.isEmpty()) {
//            if (imageDrawable != null) {
//                imageView.setImageDrawable(imageDrawable);
//            } else {
//                // Load the image from the URL using Picasso or Glide
//                Picasso.get().load(image).into(imageView);
//            }
//        } else {
//            imageView.setImageResource(R.drawable.no_image_found);
//        }
//    }

    public void loadImageIntoImageView(Context context, ImageView imageView) {
        if (image != null && !image.isEmpty()) {
            if (image.startsWith("http")) {
                // If the image is a URL, load it using Picasso
                Picasso.get().load(image).into(imageView);
            } else {
                try {
                    // Attempt to decode the image string
                    byte[] decodedBytes = Base64.decode(image, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    // Set the decoded bitmap to the ImageView
                    imageView.setImageBitmap(decodedBitmap);
                } catch (IllegalArgumentException e) {
                    // If decoding fails, set a placeholder image
                    imageView.setImageResource(R.drawable.no_image_found);
                }
            }
        } else {
            // If no image is provided, set a default image
            imageView.setImageResource(R.drawable.no_image_found);
        }
    }
}