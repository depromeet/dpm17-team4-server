package depromeet.lessonfour.server.recordquery.app.dto;

import java.util.List;

public record HomeResponseDto(
    int toiletRecordCount,
    boolean hasActivityRecord,
    String heroImage,
    List<String> heroBackgroundColors) {}
