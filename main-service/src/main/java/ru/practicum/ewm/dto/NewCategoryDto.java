package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data(staticConstructor = "of")
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewCategoryDto implements Serializable {
	@NotBlank
	@Size(min = 1, max = 50)
	private String name;
}