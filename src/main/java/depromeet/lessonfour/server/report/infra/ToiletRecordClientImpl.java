package depromeet.lessonfour.server.report.infra;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.app.client.ToiletRecordClient;
import depromeet.lessonfour.server.toiletrecord.app.service.ToiletRecordQueryService;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Component("reportToiletRecordClient")
@RequiredArgsConstructor
public class ToiletRecordClientImpl implements ToiletRecordClient {

  private final ToiletRecordQueryService toiletRecordQueryService;

  @Override
  public List<ToiletRecord> getToiletRecordsByDate(Long userId, LocalDate date) {
    return toiletRecordQueryService.findByDate(userId, date);
  }

  @Override
  public List<ToiletRecord> getToiletRecordsByActivityAtBetween(
      Long userId, ActivityAt startAt, ActivityAt endAt) {
    return toiletRecordQueryService.findByActivityAtBetween(userId, startAt, endAt);
  }
}
