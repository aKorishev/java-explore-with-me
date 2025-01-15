package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.entities.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

}