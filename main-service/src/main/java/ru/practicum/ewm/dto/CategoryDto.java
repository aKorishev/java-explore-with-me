package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class CategoryDto implements Serializable {
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	Long id;

	@NotBlank
	@Size(min = 1, max = 50)
	String name;
}
