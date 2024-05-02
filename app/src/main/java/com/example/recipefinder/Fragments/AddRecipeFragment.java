package com.example.recipefinder.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipefinder.Activities.MainActivity;
import com.example.recipefinder.Adapters.UserChosenIngredientsAdapter;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddRecipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddRecipeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final int CAMERA_REQUEST_CODE = 101;
    EditText addRecipeName;
    String recipeInstructions="";
    ArrayList<Ingredient> userChosenIgredientsList;
    ArrayList<Spinner> spinnerList;
    RecyclerView userIngredientsRecyclerView;
    UserChosenIngredientsAdapter adapter;
    AppCompatButton addInstructionsButton;
    AppCompatButton addPhotoButton;
    AppCompatButton saveRecipeButton;
    private FirebaseAuth mAuth;
    boolean didAddPhoto = false;
    boolean didCreate = false;

    Recipe userRecipe;


    public AddRecipeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddRecipeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddRecipeFragment newInstance(String param1, String param2) {
        AddRecipeFragment fragment = new AddRecipeFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_recipe, container, false);

        addRecipeName = view.findViewById(R.id.addRecipeNameEditText);
        Spinner dairySpinner = view.findViewById(R.id.dairySpinner);
        Spinner carbsSpinner = view.findViewById(R.id.carbsSpinner);
        Spinner meatSpinner = view.findViewById(R.id.meatSpinner);
        Spinner vegetablesSpinner = view.findViewById(R.id.vegetablesSpinner);
        Spinner fruitsSpinner = view.findViewById(R.id.fruitsSpinner);
        Spinner sugarsSpinner = view.findViewById(R.id.sugarsSpinner);
        addInstructionsButton = view.findViewById(R.id.addInstructionsButton);
        addPhotoButton = view.findViewById(R.id.addPhotoButton);
        saveRecipeButton = view.findViewById(R.id.saveRecipeButton);
        userRecipe = new Recipe("","",userChosenIgredientsList,recipeInstructions);
        userIngredientsRecyclerView = view.findViewById(R.id.userIngredientsRecyclerView);

        //Will be used in order to retrieve the correct users list and data from the realtime database.
        mAuth = FirebaseAuth.getInstance();

        String userPhone = getArguments().getString("userPhone");
        String userEmail = getArguments().getString("userEmail");
        String userPassword = getArguments().getString("userPassword");



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

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePhoto();
            }
        });

        addInstructionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecipeInstructionsDialog(userRecipe);
            }
        });

        saveRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didCreate = createUserRecipe(userRecipe, String.valueOf(addRecipeName.getText()), userRecipe.getImage(), userChosenIgredientsList, userRecipe.getInstructions());
                // After creating, logging, and saving user recipe list, add recipe to user recipe list
                if (didCreate) {
                    //Here retrieve the list and add the recipe
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    String userUID = firebaseUser.getUid();
                    //Get reference to the user's recipe list in the database
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(userUID);
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                //fetch user based on the myRef variable
                                User user = dataSnapshot.getValue(User.class);
                                if (user != null) {
                                    MainActivity mainActivity = ( MainActivity) getActivity();
                                    //in case this is the first recipe for the user, create his recipe list and add the recipe, otherwise add to his existing one
                                    List<Recipe> recipeList=new ArrayList<>();
                                    if(user.getRecipeList()!=null){
                                        recipeList = user.getRecipeList();
                                    }
                                    recipeList.add(userRecipe);
                                    mainActivity.writeDataToDataBase(userEmail,userPassword,userPhone,recipeList);
                                    Toast.makeText(requireContext(), "Recipe saved successfully", Toast.LENGTH_SHORT).show();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("userPhone", userPhone);
                                    bundle.putString("userEmail", userEmail);
                                    bundle.putString("userPassword", userPassword);
                                    didCreate=false;
                                    Navigation.findNavController(view).navigate(R.id.action_addRecipeFragment_to_userPageFragment, bundle);
                                } else {
                                    Log.d("readDataFromDataBase", "No user data found.");
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Toast.makeText(getContext(), "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        return view;
    }

    private void capturePhoto() {
        //Create a camera intent to capture the image
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Check if there's a camera app available to handle the intent
        if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        } else {
            //Handle the case where no camera app is available
            Toast.makeText(requireContext(), "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            //The image was successfully captured, set the thumbnail to the Recipe's image
            Bundle extras = data.getExtras();
            Bitmap thumbnail = (Bitmap) extras.get("data");
            //Convert Bitmap to Base64 string
            String thumbnailBase64 = bitmapToBase64(thumbnail);
            //Set the Base64 string to the Recipe's image
            userRecipe.setImage(thumbnailBase64);
            Toast.makeText(requireContext(), "Photo added successfully", Toast.LENGTH_SHORT).show();
        }
    }

    //Convert Bitmap to Base64 string
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
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

//    private boolean userAddPhoto(Recipe recipe){
//
//    }

    private boolean createUserRecipe(Recipe recipe,String title,String image,List<Ingredient> usedIngredients,String instructions){
        if(title.isEmpty()||title==""||instructions.isEmpty()||instructions==""||usedIngredients.isEmpty()||usedIngredients==null){
            Toast.makeText(requireContext(),"Please enter recipe name, instructions and ingredients",Toast.LENGTH_LONG).show();
            return false;
        }
        else{
            recipe.setTitle(title);
            recipe.setImage(image);
            recipe.setUsedIngredients(usedIngredients);
            return true;
        }
    }
    private void addRecipeInstructionsDialog(Recipe recipe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false);
        View alertDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.recipe_instructions_alert_dialog, null);
        builder.setView(alertDialogView);
        AlertDialog alert = builder.create();
        alert.show();
        EditText recipeInstructions = alertDialogView.findViewById(R.id.inputRecipeInstructionEditText);
        AppCompatButton closeButton = alertDialogView.findViewById(R.id.saveInstructionsButton);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recipe.setInstructions(String.valueOf(recipeInstructions.getText()));
                alert.dismiss();
            }
        });
    }
}