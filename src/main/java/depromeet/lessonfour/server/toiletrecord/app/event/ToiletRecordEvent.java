package depromeet.lessonfour.server.toiletrecord.app.event;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;

public record ToiletRecordEvent(Long userId, ActivityAt date) {}
