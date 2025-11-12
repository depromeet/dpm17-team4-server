package depromeet.lessonfour.server.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class DefaultRandomPicker implements RandomPicker {

  private static final int DEFAULT_COUNT = 3;

  private final Random random = new Random();

  /** Randomly pick 'count' items from the given list. */
  @Override
  public <T> List<T> pickRandomly(List<T> items, int count) {
    if (items == null || items.isEmpty()) {
      return Collections.emptyList();
    }

    List<T> shuffled = new ArrayList<>(items);
    Collections.shuffle(shuffled, random);
    return shuffled.subList(0, Math.min(count, shuffled.size()));
  }

  /** Randomly pick default number of items from the given list. */
  @Override
  public <T> List<T> pickRandomly(List<T> items) {
    return pickRandomly(items, DEFAULT_COUNT);
  }
}
