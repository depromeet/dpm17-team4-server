package depromeet.lessonfour.server.recordquery.app.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import depromeet.lessonfour.server.recordquery.app.client.ActivityRecordClient;
import depromeet.lessonfour.server.recordquery.app.client.ToiletRecordClient;
import depromeet.lessonfour.server.recordquery.app.dto.RecordExistenceListResponse;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetDailyExistencesUseCase {

  private final ActivityRecordClient activityRecordClient;
  private final ToiletRecordClient toiletRecordClient;

  public RecordExistenceListResponse getRecordExistenceList(
      Long userId, LocalDate startDate, LocalDate endDate) {
    ActivityAt start = ActivityAt.of(startDate);
    ActivityAt end = ActivityAt.of(endDate);

    List<DailyExistence> activityRecords =
        activityRecordClient.getDailyExistencesByActivityAtBetween(userId, start, end);
    List<DailyExistence> toiletRecords =
        toiletRecordClient.getDailyExistencesByActivityAtBetween(userId, start, end);

    return RecordExistenceListResponse.from(activityRecords, toiletRecords);
  }
}
