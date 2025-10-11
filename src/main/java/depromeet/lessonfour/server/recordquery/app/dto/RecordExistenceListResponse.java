package depromeet.lessonfour.server.recordquery.app.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import depromeet.lessonfour.server.common.domain.view.DailyExistenceView;

public record RecordExistenceListResponse(
    LocalDate startDate, LocalDate endDate, List<DailyExistenceResponseDto> results) {

  public static RecordExistenceListResponse from(
      List<DailyExistenceView> activityViews, List<DailyExistenceView> stoolViews) {

    Map<LocalDate, Boolean> activityMap =
        activityViews.stream()
            .collect(Collectors.toMap(DailyExistenceView::date, DailyExistenceView::exists));

    Map<LocalDate, Boolean> stoolMap =
        stoolViews.stream()
            .collect(Collectors.toMap(DailyExistenceView::date, DailyExistenceView::exists));

    Set<LocalDate> allDates = new TreeSet<>();
    allDates.addAll(activityMap.keySet());
    allDates.addAll(stoolMap.keySet());

    List<DailyExistenceResponseDto> merged =
        allDates.stream()
            .map(
                date ->
                    new DailyExistenceResponseDto(
                        date,
                        activityMap.getOrDefault(date, false),
                        stoolMap.getOrDefault(date, false)))
            .toList();

    return new RecordExistenceListResponse(
        merged.getFirst().date(), merged.getLast().date(), merged);
  }
}
