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
public class EventUpdateStatusResultDto {
    //todo одновременно обновляется один статус, поэтому держать 2 поля это дичь
    private List<ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDto> rejectedRequests;

    public static EventUpdateStatusResultDto rejectedOnly(List<ParticipationRequestDto> requests) {
        EventUpdateStatusResultDto result = new EventUpdateStatusResultDto();
        result.setRejectedRequests(requests);
        return result;
    }
}
