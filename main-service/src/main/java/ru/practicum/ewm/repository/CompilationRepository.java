package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.entities.CompilationEntity;

import java.util.List;

public interface CompilationRepository extends JpaRepository<CompilationEntity, Long> {
	List<CompilationEntity> findByPinned(boolean pinned, Pageable pageable);
}