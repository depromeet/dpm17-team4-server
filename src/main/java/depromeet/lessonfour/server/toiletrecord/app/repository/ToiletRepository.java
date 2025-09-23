package depromeet.lessonfour.server.toiletrecord.app.repository;

import java.time.LocalDate;
import java.util.List;

import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;

public interface ToiletRepository {

  List<ToiletRecord> findByDate(Long userId, LocalDate date);
}
