package depromeet.lessonfour.server.report.app.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.report.domain.entity.ToiletScore;
import depromeet.lessonfour.server.report.domain.repository.ToiletScoreRepository;
import depromeet.lessonfour.server.report.domain.vo.StoolReport;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ToiletScoreService {

  private final StoolReportService stoolReportService;
  private final ToiletScoreRepository toiletScoreRepository;

  public void saveScore(Long userId, LocalDate date) {
    StoolReport toiletReport = stoolReportService.generateDailyReport(userId, date);
    ToiletScore toiletScore = ToiletScore.of(userId, (int) toiletReport.getStoolScore(), date);
    toiletScoreRepository.save(toiletScore);
  }
}
