package depromeet.lessonfour.server.report.infra;

import static depromeet.lessonfour.server.report.domain.entity.QToiletScore.toiletScore;

import java.time.LocalDate;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

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
}
