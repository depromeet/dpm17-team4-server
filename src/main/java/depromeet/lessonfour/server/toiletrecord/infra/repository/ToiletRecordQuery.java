package depromeet.lessonfour.server.toiletrecord.infra.repository;

import static depromeet.lessonfour.server.toiletrecord.domain.entity.QToiletRecord.toiletRecord;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.common.domain.vo.DailyExistence;
import depromeet.lessonfour.server.common.domain.vo.RecordTime;
import depromeet.lessonfour.server.common.infra.DailyExistenceProjection;
import depromeet.lessonfour.server.common.infra.RecordTimeProjection;
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
                toiletRecord.activityAt.date.eq(activityAt.toDate()),
                toiletRecord.isDeleted.isFalse())
            .orderBy(toiletRecord.activityAt.time.asc())
            .fetch();

    return records == null ? List.of() : records;
  }

	public List<RecordTime> findTimesByDate(Long userId, ActivityAt activityAt) {
		List<Tuple> recordTimes =
			queryFactory
				.select(toiletRecord.id, toiletRecord.activityAt.time)
				.from(toiletRecord)
				.where(
					toiletRecord.user.id.eq(userId),
					toiletRecord.activityAt.date.eq(activityAt.toDate()),
					toiletRecord.isDeleted.isFalse())
				.orderBy(toiletRecord.activityAt.time.asc())
				.fetch();

		return recordTimes.stream()
			.map(
				t ->
					new RecordTimeProjection(
						t.get(toiletRecord.id), t.get(toiletRecord.activityAt.time)))
			.map(RecordTime.class::cast)
			.toList();
	}

  public List<DailyExistence> findDailyExistencesBetween(
      Long userId, ActivityAt startInclude, @Nullable ActivityAt endInclude) {

    List<LocalDate> dateRange = startInclude.datesUntil(endInclude);

    if (dateRange.isEmpty()) {
      return List.of();
    }

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

    // 기록이 있는 날짜만 반환 (false인 경우는 제외)
    return existingDates.stream()
        .<DailyExistence>map(date -> new DailyExistenceProjection(date, true))
        .toList();
  }

  public int countByActivityAt(Long userId, ActivityAt activityAt) {
    Long count =
        queryFactory
            .select(toiletRecord.count())
            .from(toiletRecord)
            .where(
                toiletRecord.user.id.eq(userId),
                toiletRecord.activityAt.date.eq(activityAt.toDate()),
                toiletRecord.isDeleted.isFalse())
            .fetchOne();

    return count != null ? count.intValue() : 0;
  }
}
