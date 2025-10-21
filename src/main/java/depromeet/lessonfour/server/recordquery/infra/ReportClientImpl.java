package depromeet.lessonfour.server.recordquery.infra;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.recordquery.app.client.ReportClient;
import depromeet.lessonfour.server.report.app.service.ReportQueryService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReportClientImpl implements ReportClient {

  private final ReportQueryService reportQueryService;

  @Override
  public int getScoreByActivityAt(Long userId, ActivityAt activityAt) {
    return reportQueryService.getScoreByActivityAt(userId, activityAt);
  }
}
