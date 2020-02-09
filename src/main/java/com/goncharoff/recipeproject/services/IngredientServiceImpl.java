package com.goncharoff.recipeproject.services;

import com.goncharoff.recipeproject.commands.IngredientCommand;
import com.goncharoff.recipeproject.converters.IngredientCommandToIngredient;
import com.goncharoff.recipeproject.converters.IngredientToIngredientCommand;
import com.goncharoff.recipeproject.domain.Ingredient;
import com.goncharoff.recipeproject.domain.Recipe;
import com.goncharoff.recipeproject.repositories.RecipeRepository;
import com.goncharoff.recipeproject.repositories.UnitOfMeasureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Controller
public class IngredientServiceImpl implements IngredientService {

    private final IngredientToIngredientCommand ingredientToIngredientCommand;
    private final IngredientCommandToIngredient ingredientCommandToIngredient;
    private final RecipeRepository recipeRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    @Autowired
    public IngredientServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand,
                                 RecipeRepository recipeRepository,
                                 IngredientCommandToIngredient ingredientCommandToIngredient,
                                 UnitOfMeasureRepository unitOfMeasureRepository) {
        this.ingredientToIngredientCommand = ingredientToIngredientCommand;
        this.recipeRepository = recipeRepository;
        this.ingredientCommandToIngredient = ingredientCommandToIngredient;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }


    @Override
    public IngredientCommand findByRecipeAndIngredientId(Long recipeId, Long ingredientId) {

        Optional<Recipe> recipeOptional = recipeRepository.findById(recipeId);

        Recipe recipe = recipeOptional.orElseThrow(() -> new RuntimeException("Can not find recipe with id " + recipeId));

        return recipe.getIngredients().stream()
                .filter(ingredient -> ingredient.getId().equals(ingredientId))
                .map(ingredientToIngredientCommand::convert)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can not find ingredient with id " + ingredientId));
    }

    @Override
    public IngredientCommand saveIngredientCommand(IngredientCommand ingredientCommand) {
        Optional<Recipe> optionalRecipe = recipeRepository.findById(ingredientCommand.getRecipeId());

        if (optionalRecipe.isEmpty()) {
            return new IngredientCommand();
        }

        Recipe recipe = optionalRecipe.get();

        //add new ingredient to recipe in ingredient with id doesn't exist, or updates existing one
        recipe.getIngredients().stream()
                .filter(ingredient -> ingredient.getId().equals(ingredientCommand.getId()))
                .findFirst()
                .ifPresentOrElse(
                        (ingredientFound) -> updateIngredientFromCommand(ingredientFound, ingredientCommand),
                        () -> {
                            recipe.addIngredient(Objects.requireNonNull(ingredientCommandToIngredient.convert(ingredientCommand)));
                        }
                );

        Recipe savedRecipe = recipeRepository.save(recipe);

        Ingredient savedIngredient = savedRecipe.getIngredients().stream()
                .filter(recipeIngredients -> recipeIngredients.getId().equals(ingredientCommand.getId()))
                .findFirst()
                .orElse(findUniqIngredient(savedRecipe, ingredientCommand));


        return ingredientToIngredientCommand.convert(savedIngredient);
    }

    private void updateIngredientFromCommand(Ingredient ingredientFound, IngredientCommand ingredientCommand) {
        ingredientFound.setDescription(ingredientCommand.getDescription());
        ingredientFound.setAmount(ingredientCommand.getAmount());
        if (ingredientCommand.getUnitOfMeasure() != null) {
            ingredientFound.setUnitOfMeasure(unitOfMeasureRepository.findById(ingredientCommand.getUnitOfMeasure().getId())
                    .orElseThrow(() -> new RuntimeException("UOM NOT FOUND")));
        }
    }

    //not totally safe... need to think about
    private Ingredient findUniqIngredient(Recipe recipe, IngredientCommand ingredientCommand) {
        return recipe.getIngredients().stream()
                .filter(recipeIngredients -> recipeIngredients.getDescription().equals(ingredientCommand.getDescription()))
                .filter(recipeIngredients -> recipeIngredients.getAmount().equals(ingredientCommand.getAmount()))
                //.filter(recipeIngredients -> recipeIngredients.getUnitOfMeasure().getId().equals(ingredientCommand.getUnitOfMeasure().getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can not find recipe ingredient with id " + ingredientCommand.getId()));
    }
}
