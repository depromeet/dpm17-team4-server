package depromeet.lessonfour.server.report.infra;

import org.springframework.stereotype.Repository;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.repository.ToiletScoreRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ToiletScoreRepositoryImpl implements ToiletScoreRepository {

  private final ToiletScoreQuery query;

  @Override
  public int getScoreByActivityAt(Long userId, ActivityAt date) {
    return query.getScoreByActivityAt(userId, date.toDate());
  }
}
