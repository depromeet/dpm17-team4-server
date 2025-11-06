package depromeet.lessonfour.server.report.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import depromeet.lessonfour.server.report.domain.policy.ToiletEvaluationPolicy;
import depromeet.lessonfour.server.report.domain.vo.ToiletEvaluation;
import depromeet.lessonfour.server.report.domain.vo.ToiletReport;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToiletEvaluationService {

  private final ToiletEvaluationPolicy toiletEvaluationPolicy;

  public List<ToiletEvaluation> evaluateAll(List<ToiletRecord> records) {
    return records.stream().map(record -> record.evaluatePoo(toiletEvaluationPolicy)).toList();
  }

  public ToiletReport summarize(List<ToiletRecord> records) {
    List<ToiletEvaluation> evaluations = evaluateAll(records);
    return ToiletReport.summarize(evaluations);
  }
}
