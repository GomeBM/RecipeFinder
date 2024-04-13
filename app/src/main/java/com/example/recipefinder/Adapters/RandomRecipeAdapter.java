package com.example.recipefinder.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipefinder.R;
import com.example.recipefinder.models.Ingredient;
import com.example.recipefinder.models.Recipe;

import java.util.List;

public class RandomRecipeAdapter extends RecyclerView.Adapter<RandomRecipeAdapter.RandomRecipeViewHolder> {

    List<Recipe> recipes;
    Context context;
    boolean isRecipeLiked=false;

    private OnFavoriteButtonClickListener favoriteButtonClickListener;

    public RandomRecipeAdapter(List<Recipe> recipes, Context context) {
        this.recipes = recipes;
        this.context = context;
    }

    @NonNull
    @Override
    public RandomRecipeAdapter.RandomRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflating the layout and giving the look to each item row
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.random_recipe_cards,parent,false);
        return new RandomRecipeAdapter.RandomRecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RandomRecipeAdapter.RandomRecipeViewHolder holder, int position) {
        //Assigning values to the views ive created in the ingredient_row layout file
        //Based on the position of the recycler view
        Recipe recipe = recipes.get(position);
        holder.recipeName.setText(recipe.getTitle());
        if (recipe.getMissedIngredients() != null) {
            holder.missingIngredients.setText("You are missing " + recipe.getMissedIngredients().size() + " ingredients");
        } else {
            holder.missingIngredients.setText("No missing ingredients");
        }
        recipe.loadImageIntoImageView(context,holder.recipeImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    Recipe currentRecipe = recipes.get(holder.getAdapterPosition());
                    showRecipeDetailsDialog(currentRecipe);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        //Return how many items my recycler view has
        return recipes.size();
    }

    public static class RandomRecipeViewHolder extends RecyclerView.ViewHolder{

        TextView recipeName, missingIngredients;
        ImageView recipeImage;

        public RandomRecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            recipeName = itemView.findViewById(R.id.randomRecipeTitle);
            recipeImage = itemView.findViewById(R.id.randomRecipeImage);
            missingIngredients = itemView.findViewById(R.id.randomRecipeMissingIngredients);
        }
    }

    private void showRecipeDetailsDialog(Recipe recipe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        View alertDialogView = LayoutInflater.from(context).inflate(R.layout.recipe_alert_dialog, null);
        builder.setView(alertDialogView);
        AlertDialog alert = builder.create();
        alert.show();
        TextView recipeTitle = alertDialogView.findViewById(R.id.dialogRecipeTitle);
        TextView recipeUsedIngredients = alertDialogView.findViewById(R.id.dialogUsedIngredients);
        TextView recipeMissedIngredients = alertDialogView.findViewById(R.id.dialogMissedIngredients);
        TextView recipeInstructions = alertDialogView.findViewById(R.id.dialogInstructions);
        ImageView recipeImage = alertDialogView.findViewById(R.id.randomRecipeImage);
        AppCompatButton closeButton = alertDialogView.findViewById(R.id.dialogCloseButton);
        AppCompatButton favouriteButton = alertDialogView.findViewById(R.id.dialogFavouriteButton);
        AppCompatButton sendEmailButton = alertDialogView.findViewById(R.id.dialogSendEmailButton);

        recipeTitle.setText(recipe.getTitle());
        recipeUsedIngredients.setText("Your ingredients:\n"+recipe.getUsedIngredientsString(recipe.getUsedIngredients()));
        if (recipe.getMissedIngredients() != null) {
            recipeMissedIngredients.setText("You are missing:\n" + recipe.getMissingIngredientsString(recipe.getMissedIngredients()));
        } else {
            recipeMissedIngredients.setText("No missing ingredients");
        }
        recipeInstructions.setText(recipe.getInstructions());
        recipe.loadImageIntoImageView(context,recipeImage);

        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                favoriteButtonClickListener.onFavoriteButtonClick(recipe);
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Construct the email content
                List<Ingredient> newIngredientList = recipe.getUsedIngredients();
                newIngredientList.addAll(recipe.getMissedIngredients());
                String subject = "New recipe from RecipeFinder!";
                String body = "Recipe Name:\n" + recipe.getTitle() + "\n\n" +
                        "Ingredients : \n" + recipe.getUsedIngredientsString(recipe.getUsedIngredients()) + "\n\n" +
                        "Instructions : \n" + recipe.getInstructions();

                // Create an Intent to send email
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:")); // Set the URI to email
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                emailIntent.putExtra(Intent.EXTRA_TEXT, body);

                // Start the activity
                context.startActivity(Intent.createChooser(emailIntent, "Send Email"));
            }

        });


    }

    public interface OnFavoriteButtonClickListener {
        void onFavoriteButtonClick(Recipe recipe);
    }

    public void setOnFavoriteButtonClickListener(OnFavoriteButtonClickListener listener) {
        this.favoriteButtonClickListener = listener;
    }

    public void resetRecipeIngredients(List<Recipe> recipes){
        for(Recipe recipe:recipes){
            List<Ingredient> newRecipeIngredients=recipe.getUsedIngredients();
            newRecipeIngredients.addAll(recipe.getMissedIngredients());
            recipe.setUsedIngredients(newRecipeIngredients);
            recipe.setUsedIngredients(null);
        }

    }

}
