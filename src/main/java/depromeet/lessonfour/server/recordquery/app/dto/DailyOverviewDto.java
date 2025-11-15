package depromeet.lessonfour.server.recordquery.app.dto;

import depromeet.lessonfour.server.report.domain.vo.toilet.ToiletEvaluationLevel;

public record DailyOverviewDto(
    ToiletEvaluationLevel toiletEvaluationLevel,
    int toiletRecordCount,
    boolean hasActivityRecord) {}
