package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDto> rejectedRequests;

    public static EventRequestStatusUpdateResult rejectedOnly(List<ParticipationRequestDto> requests) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setRejectedRequests(requests);
        return result;
    }
}
