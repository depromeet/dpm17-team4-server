package depromeet.lessonfour.server.report.domain.vo.monthly;

public record MonthlyActivityReport(int size) {

  public static MonthlyActivityReport dummy() {
    return new MonthlyActivityReport(1);
  }
}
