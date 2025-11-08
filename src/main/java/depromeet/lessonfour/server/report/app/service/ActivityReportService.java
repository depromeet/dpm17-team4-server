package depromeet.lessonfour.server.report.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.client.ActivityRecordClient;
import depromeet.lessonfour.server.report.domain.service.ActivityEvaluationService;
import depromeet.lessonfour.server.report.domain.vo.DailyActivityReport;
import depromeet.lessonfour.server.report.domain.vo.monthly.MonthlyActivityReport;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityReportService {

  private final ActivityRecordClient activityRecordClient;
  private final ActivityEvaluationService activityEvaluationService;

  public DailyActivityReport generateDailyReport(Long userId, ActivityAt activityAt) {
    List<ActivityRecord> records =
        activityRecordClient.getActivityRecordsBetween(
            userId, activityAt.getDayBefore(), activityAt.getDayAfter());

    ActivityRecord yesterday =
        records.stream()
            .filter(record -> record.getActivityAt().isDayBefore(activityAt))
            .findFirst()
            .orElse(null);

    ActivityRecord today =
        records.stream()
            .filter(record -> record.getActivityAt().isSameDate(activityAt))
            .findFirst()
            .orElse(null);

    return activityEvaluationService.evaluate(yesterday, today);
  }

  // TODO : 다른 통계치 추가하기
  public MonthlyActivityReport generateMonthlyReport(
      Long userId, ActivityAt startAt, ActivityAt endAt) {
    List<ActivityRecord> records =
        activityRecordClient.getActivityRecordsBetween(userId, startAt, endAt);
    return MonthlyActivityReport.dummy();
  }
}
