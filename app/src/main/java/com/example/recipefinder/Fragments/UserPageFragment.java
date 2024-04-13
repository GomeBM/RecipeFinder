package com.example.recipefinder.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipefinder.Adapters.RandomRecipeAdapter;
import com.example.recipefinder.Interfaces.OnCheckRecipeLikedListener;
import com.example.recipefinder.Interfaces.RecipeInteractionListener;
import com.example.recipefinder.R;
import com.example.recipefinder.Retrofit.RetrofitRequestManager;
import com.example.recipefinder.models.Recipe;
import com.example.recipefinder.models.Ingredient;
import com.example.recipefinder.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
 * Use the {@link UserPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserPageFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UserPageFragment() {
        // Required empty public constructor
    }

    ProgressDialog progressDialog;
    RetrofitRequestManager retrofitRequestManager;


    TextView recipeTitle;
    TextView availableIngredients;
    TextView ingredientsMissing;
    TextView recipeInstructions;

    FloatingActionButton addRecipeButton;
    FloatingActionButton searchByIngredientsButton;
    private List<Recipe> userRecipeList;

    private FirebaseAuth mAuth;
    TextView userNameTextView;
    RecyclerView recipesRecyclerView;
    RandomRecipeAdapter adapter;
    private RecipeInteractionListener mListener;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserPageFragment newInstance(String param1, String param2) {
        UserPageFragment fragment = new UserPageFragment();
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
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Loading...");



        retrofitRequestManager = new RetrofitRequestManager(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_page, container, false);

        addRecipeButton = view.findViewById(R.id.addRecipeButton);
        searchByIngredientsButton = view.findViewById(R.id.searchByIngredientsButton);
        userNameTextView = view.findViewById(R.id.userTextView);
        recipesRecyclerView = view.findViewById(R.id.recipesRecyclerView);
        userRecipeList = new ArrayList<>();
        mListener = new RecipeInteractionListenerImpl();

        //Will be used in order to retrieve the correct users list and data from the realtime database.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String userUID = firebaseUser.getUid();
        //here compare userRecipeList to recipeListRef


        String userPhone = getArguments().getString("userPhone");
        String userEmail = getArguments().getString("userEmail");
        String userPassword = getArguments().getString("userPassword");

        Bundle bundle = new Bundle();
        bundle.putString("userPhone", userPhone);
        bundle.putString("userEmail", userEmail);
        bundle.putString("userPassword", userPassword);


        //HERE PUT ALL ADAPTER AND RECYCLERVIEW SHIT
        adapter = new RandomRecipeAdapter(userRecipeList,requireContext());
        recipesRecyclerView.setAdapter(adapter);
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter.resetRecipeIngredients(userRecipeList);

        readDataFromDataBase(userUID);


        //Retrieve username from the database and update userTextView
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userUID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getUserName() != null) {
                        String userName = user.getUserName();
                        userNameTextView.setText(userName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to retrieve username from database", Toast.LENGTH_SHORT).show();
            }
        });

        //Attach a ValueEventListener to fetch the user's recipe list
        DatabaseReference recipeListRef = FirebaseDatabase.getInstance().getReference("users").child(userUID).child("recipeList");
        recipeListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userRecipeList.clear(); // Clear the current list
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        userRecipeList.add(recipe); // Add each recipe to the list
                    }
                }
                adapter.notifyDataSetChanged(); // Notify the adapter that the dataset has changed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to retrieve recipe list from database", Toast.LENGTH_SHORT).show();
            }
        });

        addRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_userPageFragment_to_addRecipeFragment,bundle);
            }
        });

        searchByIngredientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_userPageFragment_to_chooseIngredientsFragment,bundle);
            }
        });

        adapter.setOnFavoriteButtonClickListener(new RandomRecipeAdapter.OnFavoriteButtonClickListener() {
            @Override
            public void onFavoriteButtonClick(Recipe recipe) {
                mListener.checkIfRecipeLiked(recipe, new OnCheckRecipeLikedListener() {
                    @Override
                    public void onCheckRecipeLiked(boolean isLiked) {
                        mListener.removeRecipeFromUserList(recipe);
                    }
                });
            }
        });

        // Initialize ItemTouchHelper
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) { // Specify swipe directions
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                // Not needed for swipe-to-delete
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Called when an item is swiped
                int position = viewHolder.getAdapterPosition();
                Recipe deletedRecipe = userRecipeList.get(position);
                // Remove the swiped recipe from the list
                userRecipeList.remove(position);
                adapter.notifyItemRemoved(position);

                // You can implement any additional logic here, such as showing a confirmation dialog
                // or deleting the recipe from the database
                // For example:
                mListener.removeRecipeFromUserList(deletedRecipe);
            }
        };
        // Attach ItemTouchHelper to RecyclerView
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recipesRecyclerView);




        return view;
    }

    private void readDataFromDataBase(String userUID) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(userUID);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getRecipeList() != null) {
                        List<Recipe> recipeList = user.getRecipeList();
                        updateUI(recipeList);
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

    private void updateUI(List<Recipe> userRecipeList) {
        if (userRecipeList != null && !userRecipeList.isEmpty()) {
            userRecipeList.clear();
            userRecipeList.addAll(userRecipeList);
            adapter.notifyDataSetChanged();
        } else {
            Log.d("updateUI", "Shopping list is empty or null");
        }
    }

    // Inner class implementing RecipeInteractionListener
    private class RecipeInteractionListenerImpl implements RecipeInteractionListener {
        @Override
        public void checkIfRecipeLiked(Recipe recipe, OnCheckRecipeLikedListener listener) {
            // Implementation...

        }

        @Override
        public void removeRecipeFromUserList(Recipe recipe) {
            Log.d("TAG","inside the removeRecipeFromUser function");

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
                                        Log.d("TAG","user "+ user+"s recipe list is before removing: "+user.getRecipeList());
                                        recipeList.remove(userRecipe);
                                        Log.d("TAG","user "+ user+"s recipe list is after removing: "+user.getRecipeList());
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
            // Implementation...
        }
    }

}