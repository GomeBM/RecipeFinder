package com.example.recipefinder.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.recipefinder.Activities.MainActivity;
import com.example.recipefinder.Adapters.RandomRecipeAdapter;
import com.example.recipefinder.Adapters.UserChosenIngredientsAdapter;
import com.example.recipefinder.Interfaces.OnCheckRecipeLikedListener;
import com.example.recipefinder.Interfaces.RecipeInteractionListener;
import com.example.recipefinder.R;
import com.example.recipefinder.models.Ingredient;
import com.example.recipefinder.models.Recipe;
import com.example.recipefinder.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuggestedRecipesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuggestedRecipesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    List<Recipe> suggestedRecipes;
    RecyclerView suggestRecipesRecyclerView;
    RandomRecipeAdapter adapter;
    String userPhone,userEmail,userPassword;
    private FirebaseAuth mAuth;
    private RecipeInteractionListener mListener;

    public SuggestedRecipesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SuggestedRecipesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SuggestedRecipesFragment newInstance(String param1, String param2) {
        SuggestedRecipesFragment fragment = new SuggestedRecipesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_suggested_recipes, container, false);
        suggestRecipesRecyclerView = view.findViewById(R.id.recipesRecyclerView);
        AppCompatButton returnButton = view.findViewById(R.id.returnButton);

        mAuth = FirebaseAuth.getInstance();
        mListener = new RecipeInteractionListenerImpl();

        userPhone = getArguments().getString("userPhone");
        userEmail = getArguments().getString("userEmail");
        userPassword = getArguments().getString("userPassword");

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("recipes")) {
            Gson gson = new Gson();
            String recipesJson = bundle.getString("recipes");
            Recipe[] recipesArray = gson.fromJson(recipesJson, Recipe[].class);
            suggestedRecipes = Arrays.asList(recipesArray);
            for (Recipe deserializedRecipe:suggestedRecipes){
                Log.d("RECIPE", "RECIPE NAME:" + deserializedRecipe.getTitle());
                Log.d("RECIPE", "RECIPE USED INGREDIENTS:" + deserializedRecipe.getUsedIngredients());
                Log.d("RECIPE", "RECIPE MISSED INGREDIENTS:" + deserializedRecipe.getMissedIngredients());
                Log.d("RECIPE", "RECIPE IMAGE:" + deserializedRecipe.getImage());
                Log.d("RECIPE", "RECIPE INSTRUCTIONS:" + deserializedRecipe.getInstructions());
                Log.d("RECIPE", "-----------------------------------");
            }

        }

        adapter = new RandomRecipeAdapter(suggestedRecipes,requireContext());
        suggestRecipesRecyclerView.setAdapter(adapter);
        suggestRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle keyBundle=new Bundle();
                keyBundle.putString("userPhone", userPhone);
                keyBundle.putString("userEmail", userEmail);
                keyBundle.putString("userPassword", userPassword);
                Navigation.findNavController(view).navigate(R.id.action_suggestedRecipesFragment_to_chooseIngredientsFragment,keyBundle);
            }
        });

        adapter.setOnFavoriteButtonClickListener(new RandomRecipeAdapter.OnFavoriteButtonClickListener() {
            @Override
            public void onFavoriteButtonClick(Recipe recipe) {
                mListener.checkIfRecipeLiked(recipe, new OnCheckRecipeLikedListener() {
                    @Override
                    public void onCheckRecipeLiked(boolean isLiked) {
                        if (!isLiked) {
                            mListener.addRecipeToUserList(recipe);
                        } else {
                            mListener.removeRecipeFromUserList(recipe);
                        }
                    }
                });
            }
        });
        return view;
    }

    // Inner class implementing RecipeInteractionListener
    private class RecipeInteractionListenerImpl implements RecipeInteractionListener {
        @Override
        public void checkIfRecipeLiked(Recipe recipe, OnCheckRecipeLikedListener listener) {
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            String userUID = firebaseUser.getUid();
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(userUID);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean isLiked = false;
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            if(user.getRecipeList()==null){
                                MainActivity mainActivity = ( MainActivity) getActivity();
                                List<Recipe> recipeList=new ArrayList<>();
                                recipeList.add(recipe);
                                mainActivity.writeDataToDataBase(userEmail,userPassword,userPhone,recipeList);
                                isLiked = true;
                            }else{
                                for (Recipe checkedRecipe : user.getRecipeList()) {
                                    if (checkedRecipe.getId() == recipe.getId()) {
                                        isLiked = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            Log.d("readDataFromDataBase", "No recipe list data found.");
                        }
                    }
                    listener.onCheckRecipeLiked(isLiked);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    listener.onCheckRecipeLiked(false); // Handle error case
                }
            });
        }

        @Override
        public void removeRecipeFromUserList(Recipe recipe) {
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            String userUID = firebaseUser.getUid();
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(userUID);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            List<Recipe> recipeList = user.getRecipeList();
                            if (recipeList != null) {
                                // Iterate over the user's recipe list and remove the recipe if found
                                for (Recipe userRecipe : recipeList) {
                                    if (userRecipe.getId() == recipe.getId()) {
                                        // Remove the recipe from the list
                                        recipeList.remove(userRecipe);
                                        break; // Exit the loop once the recipe is removed
                                    }
                                }
                                // Update the user's recipe list in Firebase
                                myRef.child("recipeList").setValue(recipeList)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(requireContext(), "Recipe removed successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(requireContext(), "Failed to remove recipe", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void addRecipeToUserList(Recipe recipe) {
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            String userUID = firebaseUser.getUid();
            // Get reference to the user's recipe list in the database
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(userUID);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            MainActivity mainActivity = ( MainActivity) getActivity();
                            List<Recipe> recipeList=new ArrayList<>();
                            if(user.getRecipeList()!=null){
                                recipeList = user.getRecipeList();
                            }
                            //Now that it is favourite, dont show missed ingredients.
                            recipeList.add(recipe);
                            mainActivity.writeDataToDataBase(userEmail,userPassword,userPhone,recipeList);
                            Toast.makeText(requireContext(), "Recipe saved successfully", Toast.LENGTH_SHORT).show();
                            // Navigate back or perform any other action
                            Bundle bundle = new Bundle();
                            bundle.putString("userPhone", userPhone);
                            bundle.putString("userEmail", userEmail);
                            bundle.putString("userPassword", userPassword);
                        } else {
                            Log.d("readDataFromDataBase", "No recipe list data found.");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Toast.makeText(getContext(), "Failed to retrieve shopping list data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}