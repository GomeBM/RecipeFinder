package com.example.recipefinder.Retrofit;

import android.content.Context;
import android.util.Log;

import com.example.recipefinder.R;
import com.example.recipefinder.models.Recipe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class RetrofitRequestManager {
    private static final String BASE_URL = "https://api.spoonacular.com/";
    private static final String API_KEY = "d59e232aca0646f1a7b80114e36685c6";

    private SpoonacularAPI api;
    int stepCounter = 1;

    public RetrofitRequestManager(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SpoonacularAPI.class);
    }

    public void searchRecipesByIngredients(String ingredients,RecipeSearchCallback callback) {
        Call<List<Recipe>> call = api.searchRecipesByIngredients(ingredients, 8, API_KEY, 2, true);

        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful()) {
                    List<Recipe> recipes = response.body();
                    //Log.d("TAG", "recipes = "+recipes);
                    if (recipes != null) {
                        // Set image URL for each recipe
                        for (Recipe recipe : recipes) {
                            String imageUrl = recipe.getImage();
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                recipe.setImage(imageUrl);
                            }
                        }
                        for (Recipe recipe:recipes){
                            Log.d("RECIPE", "RECIPE NAME:" + recipe.getTitle());
                            Log.d("RECIPE", "RECIPE USED INGREDIENTS:" + recipe.getUsedIngredients());
                            Log.d("RECIPE", "RECIPE MISSED INGREDIENTS:" + recipe.getMissedIngredients());
                            Log.d("RECIPE", "RECIPE IMAGE:" + recipe.getImage());
                            Log.d("RECIPE", "-----------------------------------");
                        }
                        callback.onSuccess(recipes);
                    } else {
                        callback.onFailure("No recipes found");
                    }
                } else {
                    callback.onFailure("Failed to fetch recipes");
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }


    public void getRecipeInstructions(int id, RecipeInstructionsCallback callback) {
        Call<JsonArray> call = api.getRecipeInstructions(id, API_KEY);

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) {
                    JsonArray instructionsArray = response.body();
                    //Log.d("getRecipeInstructions", "from second api call before stringBuilder" + instructionsArray);
                    if (instructionsArray != null && instructionsArray.size() > 0) {
                        StringBuilder instructions = new StringBuilder();
                        for (JsonElement instructionElement : instructionsArray) {
                            JsonObject stepObject = instructionElement.getAsJsonObject();
                            if (stepObject.has("steps")) {
                                JsonArray stepsArray = stepObject.getAsJsonArray("steps");
                                stepCounter=1;
                                for (JsonElement stepElement : stepsArray) {
                                    JsonObject step = stepElement.getAsJsonObject();
                                    if (step.has("step")) {
                                        String stepText = step.get("step").getAsString();
                                        instructions.append(stepCounter+") ").append(stepText).append("\n");
                                        stepCounter+=1;
                                    }
                                }
                            }
                        }
                        callback.onSuccess(instructions.toString());
                    } else {
                        callback.onFailure("No instructions found");
                    }
                } else {
                    callback.onFailure("Failed to fetch instructions");
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
                Log.d("TAG", "" + t.getMessage());
            }
        });
    }
    public interface RecipeSearchCallback {
        void onSuccess(List<Recipe> recipes);
        void onFailure(String errorMessage);
    }

    public interface RecipeInstructionsCallback {
        void onSuccess(String instructions);
        void onFailure(String errorMessage);
    }

    public interface SpoonacularAPI {
        @GET("recipes/findByIngredients")
        Call<List<Recipe>> searchRecipesByIngredients(
                @Query("ingredients") String ingredients,
                @Query("number") int number,
                @Query("apiKey") String apiKey,
                @Query("ranking") int ranking,
                @Query("ignorePantry") boolean ignorePantry
        );
        @GET("recipes/{id}/analyzedInstructions")
        Call<JsonArray> getRecipeInstructions(
                @Path("id") int id,
                @Query("apiKey") String apiKey
        );
    }

}
