package ru.practicum.ewm.serices;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.entities.Category;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.serices.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public List<CategoryDto> getAll(int from, int size) {
        return categoryRepository
                .findAll(PageRequest.of(from > 0 ? from / size : 0, size))
                .stream()
                .map(Mapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto save(NewCategoryDto categoryDto) {
        Category category = categoryRepository.save(Mapper.toNewCategory(categoryDto));
        return Mapper.toCategoryDto(category);
    }

    public CategoryDto getCategory(long catId) {
        return categoryRepository
                .findById(catId)
                .map(Mapper::toCategoryDto)
                .orElseThrow(() -> new NotFoundException("Категория", catId));
    }

    @Transactional
    public void delete(long catId) {
        long categoryEvents = eventRepository.countByCategoryId(catId);
        if (categoryEvents > 0) {
            throw new IllegalStateException("The category is not empty");
        }
        if (categoryRepository.existsById(catId)) {
            categoryRepository.deleteById(catId);
        } else {
            throw new NotFoundException("Категория", catId);
        }
    }

    @Transactional
    public CategoryDto update(CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryDto.getId())
                .orElseThrow(() -> new NotFoundException("Category", categoryDto.getId()));

        category.setName(categoryDto.getName());

        categoryRepository.save(category);

        return Mapper.toCategoryDto(category);
    }
}
