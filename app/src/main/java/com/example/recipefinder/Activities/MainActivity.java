package com.example.recipefinder.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import com.example.recipefinder.R;
import com.example.recipefinder.models.Recipe;
import com.example.recipefinder.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private boolean isLoginInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
    }

    //Function for registering a new user using firebase's email and password authentication.
    public void registerNewUser(String email, String password,String name, String phone,View view) {
        //first check that all fields are indeed filled.
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill out the entire form", Toast.LENGTH_SHORT).show();
            return;
        }
        //check that the password length is compatible with googles wishes.
        if(password.length()<6){
            Toast.makeText(MainActivity.this, "Passwords must contain at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Registration success, update UI with the signedin users information
                            FirebaseUser user = mAuth.getCurrentUser();
                            //Recipe defaultRecipe = new Recipe("default");
                            List<Recipe> defaultList = new ArrayList<>();
                            //defaultList.add(defaultRecipe);
                            Toast.makeText(MainActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).navigate(R.id.action_registerFragment_to_loginFragment);
                            writeDataToDataBaseFirstTime(email, password, name, phone, defaultList);
                        } else {
                            //If registration in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void loginUser(String email, String password, String userPhone, View view){
        //first check that all fields are indeed filled.
        if (email.isEmpty() || password.isEmpty() || userPhone.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill out the entire form", Toast.LENGTH_SHORT).show();
            return;
        }
        //Check to handle multiple clicks on the login button before actually logging in, in order for the app not to crash.
        if(!isLoginInProgress){
            isLoginInProgress=true;
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            isLoginInProgress = false;
                            if (task.isSuccessful()) {
                                //Sign in success, update UI with the signed-in user's information
                                Bundle bundle = new Bundle();
                                bundle.putString("userPhone", userPhone);
                                bundle.putString("userEmail", email);
                                bundle.putString("userPassword", password);
                                Toast.makeText(MainActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                                //Navigation.findNavController(view).navigate(R.id.action_fragmentLogin_to_fragmentUser,bundle);
                                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_userPageFragment,bundle);

                            } else {
                                //If sign in fails, display a message to the user.
                                Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else {
            //If the user is impatient, display a toast indicating that login request is in progress
            Toast.makeText(MainActivity.this, "Please wait, login in progress...", Toast.LENGTH_SHORT).show();
        }
    }

    //Function for writing a new user into the realtime data base, based on the users unique ID given on the registerNewUser function.
    public void writeDataToDataBaseFirstTime(String userEmail, String userPassword, String userName,
                                             String userPhone, List<Recipe> userRecipeList) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String userUID = firebaseUser.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(userUID);
        User user = new User(userEmail, userPassword, userRecipeList, userPhone, userName);
        myRef.setValue(user);
    }

    //Function for rewriting data into an existing user inside the realtime database, based on said users unique ID.
    public void writeDataToDataBase(String userEmail, String userPassword, String userPhone, List<Recipe> userRecipeList) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String userUID = firebaseUser.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(userUID);

        //Retrieve Data object from the database
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Retrieve Data object
                    User userData = dataSnapshot.getValue(User.class);
                    if (userData != null) {
                        //Extract userName from the Data object
                        String userName = userData.getUserName();
                        //Create updated Data object with retrieved userName
                        User user = new User(userEmail, userPassword, userRecipeList, userPhone, userName);
                        //Update data in the database
                        myRef.setValue(user);
                    }
                } else {
                    //Handle the case where Data object does not exist
                    Toast.makeText(MainActivity.this, "User not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Handle error
                Toast.makeText(MainActivity.this, "Failed to retrieve data from database", Toast.LENGTH_SHORT).show();
            }
        });
    }
}