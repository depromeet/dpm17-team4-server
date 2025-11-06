package depromeet.lessonfour.server.report.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.report.domain.policy.ToiletEvaluationPolicy;
import depromeet.lessonfour.server.report.domain.vo.DailyToiletReport;
import depromeet.lessonfour.server.report.domain.vo.ToiletEvaluation;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletEvaluationService {

  private final ToiletEvaluationPolicy toiletEvaluationPolicy;

  public DailyToiletReport summarize(List<ToiletRecord> records) {
    List<ToiletEvaluation> evaluations =
        records.stream().map(toiletEvaluationPolicy::evaluate).toList();

    return DailyToiletReport.summarize(evaluations);
  }
}
