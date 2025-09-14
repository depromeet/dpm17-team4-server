package depromeet.lessonfour.server.activityrecords.services;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.activityrecords.adapters.ActivityRecordsRepository;
import depromeet.lessonfour.server.activityrecords.schemas.request.CreateActivityRecordsRequest;
import depromeet.lessonfour.server.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class CreateActivityRecordsUseCase {

  private final ActivityRecordsRepository activityRecordsRepository;

  public void createActivityRecords(CreateActivityRecordsRequest dto) {}
}
