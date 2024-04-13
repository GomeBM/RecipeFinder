package com.example.recipefinder.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipefinder.R;
import com.example.recipefinder.models.Ingredient;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class UserChosenIngredientsAdapter extends RecyclerView.Adapter<UserChosenIngredientsAdapter.MyViewHolder> {

    ArrayList<Ingredient> userChosenIngredients;
    Context context;

    public UserChosenIngredientsAdapter(ArrayList<Ingredient> userChosenIngredients, Context context) {
        this.userChosenIngredients = userChosenIngredients;
        this.context = context;
    }

    @NonNull
    @Override
    public UserChosenIngredientsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflating the layout and giving the look to each item row
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.igredient_row,parent,false);
        return new UserChosenIngredientsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserChosenIngredientsAdapter.MyViewHolder holder, int position) {
        //Assigning values to the views ive created in the ingredient_row layout file
        //Based on the position of the recycler view
        holder.ingredientName.setText(userChosenIngredients.get(position).getName());
        holder.removeIngredientImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get the current position of the item
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    //Remove the item from the list
                    userChosenIngredients.remove(adapterPosition);
                    //Notify adapter about the removed item
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        //Return how many items my recycler view has
        return userChosenIngredients.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        //Grabbing the views from my ingredient_row layout file
        //Kinda like in the onCreate method

        TextView ingredientName;
        ImageView removeIngredientImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientName = itemView.findViewById(R.id.IngredientName);
            removeIngredientImage = itemView.findViewById(R.id.removeIngredientButton);
        }
    }
}
