package depromeet.lessonfour.server.activityrecord.infra.repository;

import static depromeet.lessonfour.server.activityrecord.domain.entity.QActivityRecord.activityRecord;
import static depromeet.lessonfour.server.activityrecord.domain.entity.QFoodRecord.foodRecord;
import static depromeet.lessonfour.server.food.domain.entity.QFood.food;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class QueryDslActivityRecordRepository {

  private final JPAQueryFactory queryFactory;

  public boolean existsByUserIdAndActivityAt(Long userId, LocalDate activityAt) {
    LocalDateTime startOfDay = activityAt.atStartOfDay();
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

    Long id =
        queryFactory
            .select(activityRecord.id)
            .from(activityRecord)
            .where(
                activityRecord.user.id.eq(userId),
                activityRecord.activityAt.goe(startOfDay),
                activityRecord.activityAt.lt(endOfDay),
                activityRecord.isDeleted.eq(false))
            .fetchFirst();

    if (id == null) {
      return Optional.empty();
    }

    ActivityRecord record =
        queryFactory
            .selectFrom(activityRecord)
            .distinct()
            .leftJoin(activityRecord.foodRecords, foodRecord)
            .fetchJoin()
            .leftJoin(foodRecord.food, food)
            .fetchJoin()
            .where(activityRecord.id.eq(id), activityRecord.isDeleted.eq(false))
            .fetchOne();

    return Optional.ofNullable(record);
  }

  public List<ActivityRecord> findDayAndDayBefore(Long userId, LocalDate day) {
    LocalDateTime yesterdayStart = day.minusDays(1).atStartOfDay();
    LocalDateTime tomorrowStart = day.plusDays(1).atStartOfDay();

    return queryFactory
        .selectFrom(activityRecord)
        .where(
            activityRecord.user.id.eq(userId),
            activityRecord.activityAt.goe(yesterdayStart),
            activityRecord.activityAt.lt(tomorrowStart),
            activityRecord.isDeleted.eq(false))
        .fetch();
  }
}
