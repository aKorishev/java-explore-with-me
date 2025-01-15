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
    private List<RequestDto> confirmedRequests;
    private List<RequestDto> rejectedRequests;

    public static EventUpdateStatusResultDto rejectedOnly(List<RequestDto> requests) {
        EventUpdateStatusResultDto result = new EventUpdateStatusResultDto();
        result.setRejectedRequests(requests);
        return result;
    }
}
