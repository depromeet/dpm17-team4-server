package depromeet.lessonfour.server.report.app.dto.response;

import depromeet.lessonfour.server.toiletrecord.domain.vo.ToiletColor;

public record ToiletColorCount(ToiletColor color, int count) {}
