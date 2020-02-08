package com.goncharoff.recipeproject.services;

import com.goncharoff.recipeproject.commands.IngredientCommand;

public interface IngredientService {
    IngredientCommand findByRecipeAndIngredientId(Long recipeId, Long ingredientId);
}
