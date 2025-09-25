package depromeet.lessonfour.server.report.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.report.domain.policy.StoolEvaluationPolicy;
import depromeet.lessonfour.server.report.domain.vo.StoolEvaluation;
import depromeet.lessonfour.server.report.domain.vo.StoolReport;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoolEvaluationService {

  private final StoolEvaluationPolicy stoolEvaluationPolicy;

  public List<StoolEvaluation> evaluateAll(List<ToiletRecord> records) {
    return records.stream().map(record -> record.evaluatePoo(stoolEvaluationPolicy)).toList();
  }

  public StoolReport summarize(List<ToiletRecord> records) {
    List<StoolEvaluation> evaluations = evaluateAll(records);
    return StoolReport.summarize(evaluations);
  }
}
