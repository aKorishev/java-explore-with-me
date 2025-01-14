package ru.practicum.ewm.serices;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.CategoryToAddDto;
import ru.practicum.ewm.entities.CategoryEntity;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;

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
    public CategoryDto save(CategoryToAddDto categoryDto) {
        CategoryEntity categoryEntity = categoryRepository.save(Mapper.toNewCategory(categoryDto));
        return Mapper.toCategoryDto(categoryEntity);
    }

    public CategoryDto getCategory(long catId) {
        return categoryRepository
                .findById(catId)
                .map(Mapper::toCategoryDto)
                .orElseThrow(() -> new NotFoundException("Категория", catId));
    }

    @Transactional
    public void delete(long catId) {
        if (eventRepository.existsByCategoryEntityId(catId)) {
            throw new IllegalStateException("The category is not empty");
        }
        if (categoryRepository.existsById(catId)) {
            categoryRepository.deleteById(catId);
        } else {
            throw new NotFoundException("Категория", catId);
        }
    }

    @Transactional
    public CategoryDto update(long catId, CategoryDto categoryDto) {
        CategoryEntity entity = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category", catId));

        entity.setName(categoryDto.getName());

        categoryRepository.saveAndFlush(entity);

        return Mapper.toCategoryDto(entity);
    }
}
