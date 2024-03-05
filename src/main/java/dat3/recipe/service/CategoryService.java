package dat3.recipe.service;

import dat3.recipe.dto.CategoryDto;
import dat3.recipe.entity.Category;
import dat3.recipe.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<String> getAllCategories() {
        List<Category> categories =  categoryRepository.findAll();
        //Convert from list of Categories to DTO-type, list of Strings
        return categories.stream().map((c)->new String(c.getName())).toList();
    }

    public List<String> addCategory(CategoryDto request) {
        Optional<Category> category = categoryRepository.findByName(request.name());
        if (category.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The category already exist.");
        }
        Category newCategory = new Category();
        newCategory.setName(request.name());
        categoryRepository.save(newCategory);
        return List.of(request.name());
    }
}

