package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}