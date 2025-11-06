package depromeet.lessonfour.server.report.domain.vo.monthly;

public record ToiletPainDistribution(
    int veryLow, int low, int medium, int high, int veryHigh, int painDiff) {}
