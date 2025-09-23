package depromeet.lessonfour.server.report.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.report.domain.policy.PooEvaluationPolicy;
import depromeet.lessonfour.server.report.domain.vo.PooEvaluation;
import depromeet.lessonfour.server.report.domain.vo.PooReport;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PooEvaluationService {

  private final PooEvaluationPolicy pooEvaluationPolicy;

  public List<PooEvaluation> evaluateAll(List<ToiletRecord> records) {
    return records.stream().map(record -> record.evaluatePoo(pooEvaluationPolicy)).toList();
  }

  public PooReport summarize(List<ToiletRecord> records) {
    List<PooEvaluation> evaluations = evaluateAll(records);
    return PooReport.summarize(evaluations);
  }
}
