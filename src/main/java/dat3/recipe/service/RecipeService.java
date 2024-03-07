package dat3.recipe.service;

import dat3.recipe.dto.RecipeDto;
import dat3.recipe.entity.Category;
import dat3.recipe.entity.Recipe;
import dat3.recipe.repository.CategoryRepository;
import dat3.recipe.repository.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.*;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;

    public RecipeService(RecipeRepository recipeRepository, CategoryRepository categoryRepository) {
        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<RecipeDto> getAllRecipes(String category) {
        List<Recipe> recipes = category == null ? recipeRepository.findAll() : recipeRepository.findByCategoryName(category);
        return recipes.stream().map((r) -> new RecipeDto(r,false)).toList();
    }

    public RecipeDto getRecipeById(int idInt) {
        Recipe recipe = recipeRepository.findById(idInt).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        return new RecipeDto(recipe,false);
    }

    public RecipeDto addRecipe(RecipeDto request,Principal principal) {
        if (request.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot provide the id for a new recipe");
        }
        Category category = categoryRepository.findByName(request.getCategory()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Only existing categories are allowed"));
        Recipe newRecipe = new Recipe();
        updateRecipe(newRecipe, request, category);
        newRecipe.setOwner(principal.getName());
        recipeRepository.save(newRecipe);
        return new RecipeDto(newRecipe,false);
    }

    public RecipeDto editRecipe(RecipeDto request, int id,Principal principal) {
        if (request.getId() != id) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot change the id of an existing recipe");
        }
        Category category = categoryRepository.findByName(request.getCategory()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Only existing categories are allowed"));

        Recipe recipeToEdit = recipeRepository.findById(id).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));

        boolean isOwner = principal.getName().equals(recipeToEdit.getOwner());

        if (isOwner || isAdmin(principal)) {
            updateRecipe(recipeToEdit,request, category);
            recipeRepository.save(recipeToEdit);
            return new RecipeDto(recipeToEdit,false);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to change the Recipe");
        }
    }

    public ResponseEntity<?> deleteRecipe(int id, Principal principal) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        boolean isOwner = principal.getName().equals(recipe.getOwner());
        if (isOwner || isAdmin(principal)) {
            recipeRepository.delete(recipe);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to change the Recipe");
        }
    }

    private void updateRecipe(Recipe original, RecipeDto r, Category category) {
        original.setName(r.getName());
        original.setInstructions(r.getInstructions());
        original.setIngredients(r.getIngredients());
        original.setThumb(r.getThumb());
        original.setYouTube(r.getYouTube());
        original.setSource(r.getSource());
        original.setCategory(category);
    }

    private boolean isAdmin(Principal principal) {
        Collection<? extends GrantedAuthority> userRoles = ((Authentication) principal).getAuthorities();
        return userRoles.stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));
    }

}

