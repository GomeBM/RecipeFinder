package com.example.recipefinder.Interfaces;

import com.example.recipefinder.models.Recipe;

public interface RecipeInteractionListener{
    void checkIfRecipeLiked(Recipe recipe, OnCheckRecipeLikedListener listener);
    void removeRecipeFromUserList(Recipe recipe);
    void addRecipeToUserList(Recipe recipe);
}
