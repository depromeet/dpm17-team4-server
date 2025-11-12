package depromeet.lessonfour.server.common.util;

import java.util.List;

public interface RandomPicker {

  <T> List<T> pickRandomly(List<T> items, int count);

  <T> List<T> pickRandomly(List<T> items);
}
