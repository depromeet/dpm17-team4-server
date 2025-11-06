package depromeet.lessonfour.server.report.domain.vo.monthly;

import javax.annotation.Nullable;

import depromeet.lessonfour.server.report.domain.entity.ToiletScore;

public record ScoreSummary(@Nullable ToiletScore best, @Nullable ToiletScore worst) {}
