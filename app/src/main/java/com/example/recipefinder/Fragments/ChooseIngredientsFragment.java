package com.example.recipefinder.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.recipefinder.Adapters.UserChosenIngredientsAdapter;
import com.example.recipefinder.R;
import com.example.recipefinder.Retrofit.RetrofitRequestManager;
import com.example.recipefinder.models.Ingredient;
import com.example.recipefinder.models.Recipe;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChooseIngredientsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChooseIngredientsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ArrayList<Ingredient> userChosenIgredientsList;
    ArrayList<Spinner> spinnerList;
    RecyclerView userIngredientsRecyclerView;
    UserChosenIngredientsAdapter adapter;
    RetrofitRequestManager retrofitRequestManager;
    String userPhone,userEmail,userPassword;

    private int totalRecipes = 0;
    private int fetchedCount = 0;

    public ChooseIngredientsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChooseIngredientsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChooseIngredientsFragment newInstance(String param1, String param2) {
        ChooseIngredientsFragment fragment = new ChooseIngredientsFragment();
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
        retrofitRequestManager = new RetrofitRequestManager(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_choose_ingredients, container, false);
        Button getRecipesButton = view.findViewById(R.id.getRecipesButton);
        Spinner dairySpinner = view.findViewById(R.id.dairySpinner);
        Spinner carbsSpinner = view.findViewById(R.id.carbsSpinner);
        Spinner meatSpinner = view.findViewById(R.id.meatSpinner);
        Spinner vegetablesSpinner = view.findViewById(R.id.vegetablesSpinner);
        Spinner fruitsSpinner = view.findViewById(R.id.fruitsSpinner);
        Spinner sugarsSpinner = view.findViewById(R.id.sugarsSpinner);
        userIngredientsRecyclerView = view.findViewById(R.id.userIngredientsRecyclerView);

        userPhone = getArguments().getString("userPhone");
        userEmail = getArguments().getString("userEmail");
        userPassword = getArguments().getString("userPassword");


        userChosenIgredientsList = new ArrayList<>();
        spinnerList = new ArrayList<>();
        spinnerList.add(dairySpinner);
        spinnerList.add(carbsSpinner);
        spinnerList.add(meatSpinner);
        spinnerList.add(vegetablesSpinner);
        spinnerList.add(fruitsSpinner);
        spinnerList.add(sugarsSpinner);
        attachListenersToSpinners(spinnerList);

        adapter = new UserChosenIngredientsAdapter(userChosenIgredientsList,requireContext());
        userIngredientsRecyclerView.setAdapter(adapter);
        userIngredientsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        getRecipesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //HERE CREATE A BUNDLE WITH AND PUT INSIDE OF IT THE RECIPE RECIEVED FROM THE 2API CALLS, THEN SEND THE BUNDLE WITH THE NAVIGATION
                String userIngredientStringify=convertIngredientsListToString(userChosenIgredientsList);
                performRecipeSearch(userChosenIgredientsList,userIngredientStringify);
                //Navigation.findNavController(view).navigate(R.id.action_chooseIngredientsFragment_to_userPageFragment);
            }
        });

        return view;
    }
    private void attachListenersToSpinners(List<Spinner> spinnerList) {
        for (Spinner spinner : spinnerList) {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //Handle item selection
                    if (position != AdapterView.INVALID_POSITION) {
                        String selectedItem = parent.getItemAtPosition(position).toString().trim();
                        String firstItem = parent.getItemAtPosition(0).toString();
                        //Only add the ingredient if the selected item is different from the prompt
                        if (!selectedItem.equals(firstItem)) {
                            for(Ingredient ingredient:userChosenIgredientsList){
                                if(selectedItem.equals(ingredient.getName())){
                                    Toast.makeText(requireContext(),"You already chose "+selectedItem,Toast.LENGTH_SHORT).show();
                                    parent.setSelection(0);
                                    return;
                                }
                            }
                            userChosenIgredientsList.add(new Ingredient(selectedItem));
                            parent.setSelection(0);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Handle case when nothing is selected
                }
            });
        }
    }

    private void performRecipeSearch(ArrayList<Ingredient> userIngredients,String userIngredientsStringify) {
        List<Recipe> returnedRecipes = new ArrayList<Recipe>();
        retrofitRequestManager.searchRecipesByIngredients(userIngredientsStringify, new RetrofitRequestManager.RecipeSearchCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (!recipes.isEmpty()) {
                    totalRecipes=recipes.size();
                    for(Recipe recipe:recipes) {
                        fetchRecipeInstructions(recipe,returnedRecipes);
                    }
                    Log.d("RECIPES","Size of return recipes from api calls = "+recipes.size());
                } else {
                    Toast.makeText(requireContext(), "No recipes found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void fetchRecipeInstructions(Recipe recipeFromPreviousCall, List<Recipe> returnedRecipes) {
//        retrofitRequestManager.getRecipeInstructions(recipeFromPreviousCall.getId(), new RetrofitRequestManager.RecipeInstructionsCallback() {
//            @Override
//            public void onSuccess(String instructions) {
//                recipeFromPreviousCall.setInstructions(instructions);
//                returnedRecipes.add(recipeFromPreviousCall);
//                fetchedCount++;
//                if (fetchedCount == totalRecipes) {
//                    onAllFetched(returnedRecipes);
//                }
//            }
//            @Override
//            public void onFailure(String errorMessage) {
//                Toast.makeText(requireContext(),"Failed to fetch recipe  "+recipeFromPreviousCall.getTitle(), Toast.LENGTH_SHORT).show();
//                Log.d("TAG","Failed to fetch recipe instructions for "+recipeFromPreviousCall.getTitle());
//            }
//        });
//    }

    private void fetchRecipeInstructions(Recipe recipeFromPreviousCall, List<Recipe> returnedRecipes) {
        retrofitRequestManager.getRecipeInstructions(recipeFromPreviousCall.getId(), new RetrofitRequestManager.RecipeInstructionsCallback() {
            @Override
            public void onSuccess(String instructions) {
                recipeFromPreviousCall.setInstructions(instructions);
                returnedRecipes.add(recipeFromPreviousCall);
                fetchedCount++;
                if (fetchedCount == totalRecipes) {
                    onAllFetched(returnedRecipes);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(requireContext(), "Failed to fetch recipe  " + recipeFromPreviousCall.getTitle(), Toast.LENGTH_SHORT).show();
                Log.d("TAG", "Failed to fetch recipe instructions for " + recipeFromPreviousCall.getTitle());
                fetchedCount++;
                if (fetchedCount == totalRecipes) {
                    onAllFetched(returnedRecipes);
                }
            }
        });
    }

    public String convertIngredientsListToString(ArrayList<Ingredient> ingredientsList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Ingredient ingredient : ingredientsList) {
            stringBuilder.append(ingredient.getName()).append(",");
        }
        // Remove the last comma
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    private void onAllFetched(List<Recipe> recipes) {
        // All recipes have been fetched successfully
        // You can now pass the list of recipes to the next fragment
        Gson gson = new Gson();
        String recipesJson = gson.toJson(recipes);

        Bundle bundle = new Bundle();
        bundle.putString("recipes", recipesJson);
        bundle.putString("userPhone", userPhone);
        bundle.putString("userEmail", userEmail);
        bundle.putString("userPassword", userPassword);
        Navigation.findNavController(requireView()).navigate(R.id.action_chooseIngredientsFragment_to_suggestedRecipesFragment, bundle);
    }

}