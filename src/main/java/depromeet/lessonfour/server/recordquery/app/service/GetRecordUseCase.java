package depromeet.lessonfour.server.recordquery.app.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.view.DailyExistenceView;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.recordquery.app.dto.RecordExistenceListResponse;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetRecordUseCase {

  private final ActivityRecordExistenceClient activityRecordExistenceClient;
  private final StoolRecordExistenceClient stoolRecordExistenceClient;

  public RecordExistenceListResponse getRecordExistenceList(
      Long userId, LocalDate startDate, LocalDate endDate) {
    ActivityAt start = ActivityAt.of(startDate);
    ActivityAt end = ActivityAt.of(endDate);

    List<DailyExistenceView> activityRecords =
        activityRecordExistenceClient.existsByActivityAtBetween(userId, start, end);
    List<DailyExistenceView> stoolRecords =
        stoolRecordExistenceClient.existsByActivityAtBetween(userId, start, end);

    return RecordExistenceListResponse.from(activityRecords, stoolRecords);
  }
}
