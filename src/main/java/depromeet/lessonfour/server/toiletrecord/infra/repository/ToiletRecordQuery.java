package depromeet.lessonfour.server.toiletrecord.infra.repository;

import static depromeet.lessonfour.server.toiletrecord.domain.entity.QToiletRecord.toiletRecord;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import depromeet.lessonfour.server.common.infra.DailyExistenceProjection;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ToiletRecordQuery {

  private final JPAQueryFactory queryFactory;

  public List<ToiletRecord> findByDate(Long userId, ActivityAt activityAt) {
    List<ToiletRecord> records =
        queryFactory
            .selectFrom(toiletRecord)
            .where(
                toiletRecord.user.id.eq(userId),
                toiletRecord.activityAt.date.eq(activityAt.toDate()))
            .orderBy(toiletRecord.activityAt.date.asc())
            .fetch();

    return records == null ? List.of() : records;
  }

  public List<DailyExistence> findDailyExistencesByActivityAt(
      Long userId, ActivityAt start, @Nullable ActivityAt end) {

    List<LocalDate> dateRange = start.datesUntil(end);

    List<LocalDate> existingDates =
        queryFactory
            .select(toiletRecord.activityAt.date)
            .from(toiletRecord)
            .where(
                toiletRecord.user.id.eq(userId),
                toiletRecord.activityAt.date.between(dateRange.getFirst(), dateRange.getLast()),
                toiletRecord.isDeleted.isFalse())
            .distinct()
            .fetch();

    Set<LocalDate> existingSet = new HashSet<>(existingDates);

    return dateRange.stream()
        .<DailyExistence>map(date -> new DailyExistenceProjection(date, existingSet.contains(date)))
        .toList();
  }
}
