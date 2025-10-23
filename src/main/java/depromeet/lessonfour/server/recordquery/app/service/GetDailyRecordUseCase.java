package depromeet.lessonfour.server.recordquery.app.service;

import java.time.LocalDate;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.recordquery.app.client.ActivityRecordClient;
import depromeet.lessonfour.server.recordquery.app.client.ReportClient;
import depromeet.lessonfour.server.recordquery.app.client.ToiletRecordClient;
import depromeet.lessonfour.server.recordquery.app.dto.DailyRecordResponse;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetDailyRecordUseCase {

  private final ActivityRecordClient activityRecordClient;
  private final ToiletRecordClient toiletRecordClient;
  private final ReportClient reportClient;

  public DailyRecordResponse getDailyRecord(Long userId, LocalDate date) {
    ActivityAt activityAt = ActivityAt.of(date);

    int score = reportClient.getScoreByActivityAt(userId, activityAt);
    int toiletRecordCount = toiletRecordClient.getToiletRecordCountByActivityAt(userId, activityAt);
    int activityRecordCount =
        activityRecordClient.getActivityRecordCountByActivityAt(userId, activityAt);

    return new DailyRecordResponse(score, toiletRecordCount, activityRecordCount);
  }
}
