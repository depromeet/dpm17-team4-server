package depromeet.lessonfour.server.report.domain.vo.monthly;

import depromeet.lessonfour.server.report.domain.entity.ToiletScore;
import jakarta.annotation.Nullable;

public record ScoreSummary(@Nullable ToiletScore best, @Nullable ToiletScore worst) {}
