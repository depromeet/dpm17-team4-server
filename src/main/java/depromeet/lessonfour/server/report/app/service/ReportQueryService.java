package depromeet.lessonfour.server.report.app.service;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.repository.ToiletScoreRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportQueryService {

  private final ToiletScoreRepository toiletScoreRepository;

  public int getScoreByActivityAt(Long userId, ActivityAt date) {
    return toiletScoreRepository.getScoreByActivityAt(userId, date);
  }
}
