package depromeet.lessonfour.server.recordquery.app.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import depromeet.lessonfour.server.common.domain.vo.DailyExistence;

public record RecordExistenceListResponse(
    LocalDate startDate, LocalDate endDate, List<DailyExistenceResponseDto> results) {

  public static RecordExistenceListResponse from(
      List<DailyExistence> activityExistences, List<DailyExistence> toiletExistences) {

    Map<LocalDate, Boolean> activityMap =
        activityExistences.stream()
            .collect(Collectors.toMap(DailyExistence::date, DailyExistence::exists));

    Map<LocalDate, Boolean> toiletRecordMap =
        toiletExistences.stream()
            .collect(Collectors.toMap(DailyExistence::date, DailyExistence::exists));

    Set<LocalDate> allDates = new TreeSet<>();
    allDates.addAll(activityMap.keySet());
    allDates.addAll(toiletRecordMap.keySet());

    List<DailyExistenceResponseDto> merged =
        allDates.stream()
            .map(
                date ->
                    new DailyExistenceResponseDto(
                        date,
                        activityMap.getOrDefault(date, false),
                        toiletRecordMap.getOrDefault(date, false)))
            .toList();

    if (merged.isEmpty()) {
      return new RecordExistenceListResponse(null, null, merged);
    }

    return new RecordExistenceListResponse(
        merged.getFirst().date(), merged.getLast().date(), merged);
  }
}
