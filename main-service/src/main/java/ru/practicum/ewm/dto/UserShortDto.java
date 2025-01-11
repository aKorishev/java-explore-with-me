package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor(staticName = "of")
public class UserShortDto implements Serializable {

	private final @NotNull Long id;

	private final @NotBlank String name;
}
