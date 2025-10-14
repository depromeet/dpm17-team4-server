package depromeet.lessonfour.server.recordquery.app.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.view.RecordTimeView;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.recordquery.app.dto.ToiletTimeItemDto;
import depromeet.lessonfour.server.recordquery.app.dto.ToiletTimeListResponse;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetToiletTimesUseCase {

  private final ToiletRecordTimesClient toiletRecordTimesClient;

  public ToiletTimeListResponse getToiletTimes(Long userId, LocalDate date) {
    List<RecordTimeView> views =
        toiletRecordTimesClient.findTimesByDate(userId, ActivityAt.of(date));

    var items = views.stream().map(v -> new ToiletTimeItemDto(v.id(), v.activityTime())).toList();

    return new ToiletTimeListResponse(date, items);
  }
}
