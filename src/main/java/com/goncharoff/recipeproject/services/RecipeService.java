package com.goncharoff.recipeproject.services;

import com.goncharoff.recipeproject.domain.Recipe;

import java.util.Set;

public interface RecipeService {
    Set<Recipe> getAllRecipes();
}
