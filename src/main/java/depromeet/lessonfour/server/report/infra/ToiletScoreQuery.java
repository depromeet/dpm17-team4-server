package depromeet.lessonfour.server.report.infra;

import static depromeet.lessonfour.server.report.domain.entity.QToiletScore.toiletScore;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.report.domain.entity.ToiletScore;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ToiletScoreQuery {

  private final JPAQueryFactory queryFactory;

  public int getScoreByActivityAt(Long userId, LocalDate date) {
    Integer score =
        queryFactory
            .select(toiletScore.score)
            .from(toiletScore)
            .where(toiletScore.userId.eq(userId), toiletScore.date.eq(date))
            .fetchOne();

    return score != null ? score : 0;
  }

  public Optional<ToiletScore> findMaxScoreBetween(Long userId, ActivityAt start, ActivityAt end) {
    ToiletScore score =
        queryFactory
            .selectFrom(toiletScore)
            .where(
                toiletScore.userId.eq(userId),
                toiletScore.date.goe(start.toDate()),
                toiletScore.date.lt(end.toDate()))
            .orderBy(toiletScore.score.desc())
            .fetchFirst();

    return Optional.ofNullable(score);
  }

  public Optional<ToiletScore> findMinScoreBetween(Long userId, ActivityAt start, ActivityAt end) {
    ToiletScore score =
        queryFactory
            .selectFrom(toiletScore)
            .where(
                toiletScore.userId.eq(userId),
                toiletScore.date.goe(start.toDate()),
                toiletScore.date.lt(end.toDate()))
            .orderBy(toiletScore.score.asc())
            .fetchFirst();

    return Optional.ofNullable(score);
  }
}
