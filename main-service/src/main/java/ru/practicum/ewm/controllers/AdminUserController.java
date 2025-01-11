package ru.practicum.ewm.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.practicum.ewm.serices.UserService;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/admin/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AdminUserController {
	private final UserService userService;

	@GetMapping
	public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
								  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
								  @RequestParam(defaultValue = "10") @Positive int size) {
		if (ids == null || ids.isEmpty()) {
			return userService.getAllUsers(from, size);
		} else {
			return userService.getUsersById(ids);
		}
	}

	@PostMapping
	@ResponseStatus(code = HttpStatus.CREATED)
	public UserDto registerUser(@RequestBody @Valid NewUserRequest newUserRequest) {
		return userService.addUser(newUserRequest);
	}

	@DeleteMapping("/{userId}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long userId) {
		userService.delete(userId);
	}
}
