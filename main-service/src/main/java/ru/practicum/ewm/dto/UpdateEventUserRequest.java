package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateEventUserRequest extends UpdateEventBaseRequest {
	private @Nullable StateAction stateAction;

	@JsonIgnore
	public boolean isStateNeedUpdate() {
		return stateAction != null;
	}

	@Override
	public boolean isNeedAnyUpdates() {
		return super.isNeedAnyUpdates() || isStateNeedUpdate();
	}

	public enum StateAction {
		SEND_TO_REVIEW,
		CANCEL_REVIEW
	}
}
