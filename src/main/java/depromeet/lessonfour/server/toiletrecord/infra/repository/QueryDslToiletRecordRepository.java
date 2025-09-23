package depromeet.lessonfour.server.toiletrecord.infra.repository;

import static depromeet.lessonfour.server.toiletrecord.domain.entity.QToiletRecord.toiletRecord;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class QueryDslToiletRecordRepository {

  private final JPAQueryFactory queryFactory;

  public List<ToiletRecord> findByDate(Long userId, LocalDate date) {
    List<ToiletRecord> records =
        queryFactory
            .selectFrom(toiletRecord)
            .where(
                toiletRecord.user.id.eq(userId),
                toiletRecord.occurredAt.goe(date.atStartOfDay()),
                toiletRecord.occurredAt.lt(date.atStartOfDay().plusDays(1)))
            .fetch();

    return records == null ? List.of() : records;
  }
}
