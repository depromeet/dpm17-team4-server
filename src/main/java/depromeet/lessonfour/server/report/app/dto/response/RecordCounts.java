package depromeet.lessonfour.server.report.app.dto.response;

public record RecordCounts(int activityCount, int toiletCount, int totalCount) {

  public static RecordCounts of(int activityCount, int toiletCount) {
    return new RecordCounts(activityCount, toiletCount, activityCount + toiletCount);
  }
}
