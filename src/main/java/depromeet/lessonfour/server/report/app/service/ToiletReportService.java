package depromeet.lessonfour.server.report.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.client.ToiletRecordClient;
import depromeet.lessonfour.server.report.domain.vo.toilet.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.MonthlyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletEvaluation;
import depromeet.lessonfour.server.report.domain.vo.toilet.WeeklyToiletReport;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletReportService {

  private final ToiletRecordClient toiletRecordClient;

  /** 일간 배변 리포트 생성 */
  public DailyToiletReport generateDailyReport(Long userId, ActivityAt activityAt) {

    List<ToiletRecord> dailyRecords =
        toiletRecordClient.getToiletRecordsByActivityAt(userId, activityAt);

    return ToiletEvaluation.summarize(dailyRecords);
  }

  /** 주간 배변 리포트 생성 */
  public WeeklyToiletReport generateWeeklyReport(
      Long userId, ActivityAt start, ActivityAt endExclusive) {

    List<ToiletRecord> records =
        toiletRecordClient.getToiletRecordsByActivityAtBetween(userId, start, endExclusive);

    return WeeklyToiletReport.summarize(records);
  }

  /** 월간 배변 리포트 생성 */
  public MonthlyToiletReport generateMonthlyReport(
      Long userId, ActivityAt start, ActivityAt endExclusive) {

    List<ToiletRecord> lastMonthToiletRecords =
        toiletRecordClient.getToiletRecordsByActivityAtBetween(userId, start.getLastMonth(), start);

    // 지난 달 통증 일수 계산
    long lastMonthPainfulDays =
        lastMonthToiletRecords.stream().filter(ToiletRecord::isPainful).count();

    // 이번 달 기록 가져오기
    List<ToiletRecord> records =
        toiletRecordClient.getToiletRecordsByActivityAtBetween(userId, start, endExclusive);

    return MonthlyToiletReport.summarize(records, lastMonthPainfulDays);
  }
}
