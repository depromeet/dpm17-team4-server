package depromeet.lessonfour.server.recordquery.app.client;

import java.util.List;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;

public interface ToiletRecordClient {

  List<DailyExistence> getDailyExistencesBetween(
      Long userId, ActivityAt startInclude, ActivityAt endInclude);

  int getToiletRecordCountByActivityAt(Long userId, ActivityAt activityAt);

  HeroAssets getToiletHeroAssets(int score);

  record HeroAssets(String image, List<String> backgroundColors) {}
}
