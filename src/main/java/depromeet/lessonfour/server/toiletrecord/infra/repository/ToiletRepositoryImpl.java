package depromeet.lessonfour.server.toiletrecord.infra.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import depromeet.lessonfour.server.toiletrecord.app.repository.ToiletRepository;
import depromeet.lessonfour.server.toiletrecord.domain.entity.ToiletRecord;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ToiletRepositoryImpl implements ToiletRepository {

  @Override
  public List<ToiletRecord> findByDate(Long userId, LocalDate date) {
    return List.of();
  }
}
