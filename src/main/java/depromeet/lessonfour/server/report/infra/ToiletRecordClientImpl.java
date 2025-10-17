package depromeet.lessonfour.server.report.infra;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.report.app.client.ToiletRecordClient;
import depromeet.lessonfour.server.toiletrecord.app.service.ToiletRecordQueryService;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ToiletRecordClientImpl implements ToiletRecordClient {

  private final ToiletRecordQueryService toiletRecordQueryService;

  @Override
  public List<ToiletRecord> getToiletRecordsByDate(Long userId, LocalDate date) {
    return toiletRecordQueryService.findByDate(userId, date);
  }
}
