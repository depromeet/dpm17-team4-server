package depromeet.lessonfour.server.report.domain.vo;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ReportResult implements Iterable<Map.Entry<ReportType, Object>> {

  private final Map<ReportType, Object> results = new LinkedHashMap<>();
  private final ReportPeriod period;

  public ReportResult(ReportPeriod period) {
    this.period = period;
  }

  public void add(ReportType type, Object value) {
    results.put(type, value);
  }

  public Object get(ReportType type) {
    return results.get(type);
  }

  @Override
  public Iterator<Entry<ReportType, Object>> iterator() {
    return results.entrySet().iterator();
  }
}
