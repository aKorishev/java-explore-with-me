package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor(staticName = "of")
public class UserToAddDto implements Serializable {

	@NotBlank
	@Size(min = 2, max = 250)
	private final String name;

	@Email
	@NotBlank
	@Size(min = 6, max = 254)
	private final String email;
}