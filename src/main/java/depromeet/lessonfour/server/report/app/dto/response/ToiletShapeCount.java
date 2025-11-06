package depromeet.lessonfour.server.report.app.dto.response;

import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletShape;

public record ToiletShapeCount(ToiletShape shape, int count) {}
