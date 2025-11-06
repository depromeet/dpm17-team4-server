package depromeet.lessonfour.server.report.app.dto.response;

import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;

public record RecordCounts(int activityCount, int toiletCount, int totalCount) {

  public RecordCounts {
    if (totalCount != activityCount + toiletCount) {
      throw new ServerException(ErrorCode.INVALID_FIELD_ERROR);
    }
  }

  public static RecordCounts of(int activityCount, int toiletCount) {
    return new RecordCounts(activityCount, toiletCount, activityCount + toiletCount);
  }
}
