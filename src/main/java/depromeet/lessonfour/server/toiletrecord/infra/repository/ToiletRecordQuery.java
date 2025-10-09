package depromeet.lessonfour.server.toiletrecord.infra.repository;

import static depromeet.lessonfour.server.activityrecord.domain.entity.QActivityRecord.activityRecord;
import static depromeet.lessonfour.server.toiletrecord.domain.entity.QToiletRecord.toiletRecord;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import depromeet.lessonfour.server.common.domain.vo.ActivityAt;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
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
                activityRecord.activityAt.date.eq(activityAt.toDate()))
            .orderBy(toiletRecord.activityAt.date.asc())
            .fetch();

    return records == null ? List.of() : records;
  }
}
