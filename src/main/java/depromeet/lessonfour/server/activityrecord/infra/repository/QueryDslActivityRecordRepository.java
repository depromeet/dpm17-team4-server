package depromeet.lessonfour.server.activityrecord.infra.repository;

import static depromeet.lessonfour.server.activityrecord.domain.entity.QActivityRecord.activityRecord;
import static depromeet.lessonfour.server.activityrecord.domain.entity.QFoodRecord.foodRecord;
import static depromeet.lessonfour.server.food.domain.entity.QFood.food;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class QueryDslActivityRecordRepository {

  private final JPAQueryFactory queryFactory;

  public boolean existsByUserIdAndActivityAt(Long userId, LocalDateTime activityAt) {
    LocalDateTime startOfDay = activityAt.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    Integer count =
        queryFactory
            .selectOne()
            .from(activityRecord)
            .where(
                activityRecord.user.id.eq(userId),
                activityRecord.activityAt.goe(startOfDay),
                activityRecord.activityAt.lt(endOfDay),
                activityRecord.isDeleted.eq(false))
            .fetchFirst();
    return count != null;
  }

  public Optional<ActivityRecord> findByUserIdAndOccurredAt(Long userId, LocalDate date) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

    ActivityRecord record =
        queryFactory
            .selectFrom(activityRecord)
            .leftJoin(activityRecord.foodRecords, foodRecord)
            .fetchJoin()
            .leftJoin(foodRecord.food, food)
            .fetchJoin()
            .where(
                activityRecord.user.id.eq(userId),
                activityRecord.activityAt.goe(startOfDay),
                activityRecord.activityAt.lt(endOfDay),
                activityRecord.isDeleted.eq(false))
            .distinct()
            .orderBy(activityRecord.activityAt.desc())
            .fetchFirst();

    return Optional.ofNullable(record);
  }
}
