package ru.practicum.ewm.serices;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.UserToAddDto;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.entities.UserEntity;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
	private final UserRepository userRepository;

	public List<UserDto> getAllUsers(int from, int size) {
		return userRepository.findAll(page(from, size))
				.stream()
				.map(Mapper::toUserDto)
				.collect(Collectors.toList());
	}

	public List<UserDto> getUsersById(List<Long> ids) {
		return userRepository.findAllById(ids)
				.stream()
				.map(Mapper::toUserDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public UserDto addUser(UserToAddDto userToAddDto) {
		var entity = new UserEntity();
		entity.setName(userToAddDto.getName());
		entity.setEmail(userToAddDto.getEmail());

		userRepository.saveAndFlush(entity);
		return Mapper.toUserDto(entity);
	}

	@Transactional
	public void delete(long userId) {
		if (userRepository.existsById(userId)) {
			userRepository.deleteById(userId);
		} else {
			throw new NotFoundException("Пользователь", userId);
		}
	}

	private static PageRequest page(int from, int size) {
		return PageRequest.of(from > 0 ? from / size : 0, size);
	}
}
