package depromeet.lessonfour.server.activityrecord.infra.repository;

import static depromeet.lessonfour.server.activityrecord.domain.entity.QActivityRecord.activityRecord;
import static depromeet.lessonfour.server.activityrecord.domain.entity.QFoodRecord.foodRecord;
import static depromeet.lessonfour.server.food.domain.entity.QFood.food;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import depromeet.lessonfour.server.common.infra.DailyExistenceProjection;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ActivityRecordQuery {

  private final JPAQueryFactory queryFactory;

  public Optional<ActivityRecord> findByDate(Long userId, ActivityAt activityAt) {
    Long id =
        queryFactory
            .select(activityRecord.id)
            .from(activityRecord)
            .where(
                activityRecord.userId.eq(userId),
                activityRecord.activityAt.date.eq(activityAt.toDate()),
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

  public List<ActivityRecord> findByActivityAtBetween(
      Long userId, ActivityAt start, ActivityAt end) {

    return queryFactory
        .selectFrom(activityRecord)
        .distinct()
        .leftJoin(activityRecord.foodRecords, foodRecord)
        .fetchJoin()
        .leftJoin(foodRecord.food, food)
        .fetchJoin()
        .where(
            activityRecord.userId.eq(userId),
            activityRecord.activityAt.date.goe(start.toDate()),
            activityRecord.activityAt.date.lt(end.toDate()),
            activityRecord.isDeleted.eq(false))
        .fetch();
  }

  public List<DailyExistence> findDailyExistencesBetween(
      Long userId, ActivityAt start, @Nullable ActivityAt end) {

    List<LocalDate> dateRange = start.datesUntil(end);

    if (dateRange.isEmpty()) {
      return List.of();
    }

    List<LocalDate> existingDates =
        queryFactory
            .select(activityRecord.activityAt.date)
            .from(activityRecord)
            .where(
                activityRecord.userId.eq(userId),
                activityRecord.activityAt.date.between(dateRange.getFirst(), dateRange.getLast()),
                activityRecord.isDeleted.isFalse())
            .distinct()
            .fetch();

    return existingDates.stream()
        .<DailyExistence>map(date -> new DailyExistenceProjection(date, true))
        .toList();
  }
}
