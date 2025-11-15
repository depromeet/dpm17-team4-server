package depromeet.lessonfour.server.recordquery.api.dto;

import java.util.List;

public record HomeOverviewResponse(
    int toiletRecordCount,
    boolean hasActivityRecord,
    String heroImage,
    List<String> heroBackgroundColors) {}
