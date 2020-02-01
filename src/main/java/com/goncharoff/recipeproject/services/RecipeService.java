package com.goncharoff.recipeproject.services;

import com.goncharoff.recipeproject.domain.Recipe;

import java.util.List;
import java.util.Optional;

public interface RecipeService {
    List<Recipe> getAllRecipes();
}
