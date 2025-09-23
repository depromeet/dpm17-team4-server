package depromeet.lessonfour.server.report.app.support;

import depromeet.lessonfour.server.activityrecord.app.service.ActivityRecordQueryService;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.report.domain.vo.ReportPeriod;
import depromeet.lessonfour.server.toiletrecord.app.service.ToiletRecordQueryService;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@RequiredArgsConstructor
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ReportDataProvider {

  private final ActivityRecordQueryService activityRecordQueryService;
  private final ToiletRecordQueryService toiletRecordQueryService;

  private List<ActivityRecord> activitiesCache;
  private List<ToiletRecord> toiletsCache;

  public List<ActivityRecord> getActivities(Long userId, ReportPeriod period) {
    if (activitiesCache == null) {
      activitiesCache = activityRecordQueryService.findByUserAndPeriod(userId, period.start(), period.end());
    }
    return activitiesCache;
  }

  public List<ToiletRecord> getToiletRecords(Long userId, ReportPeriod period) {
    if (toiletsCache == null) {
      toiletsCache = toiletRecordQueryService.findByUserAndPeriod(userId, period.start(), period.end());
    }
    return toiletsCache;
  }
}
