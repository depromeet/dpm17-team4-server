package depromeet.lessonfour.server.activityrecord.app.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.activityrecord.app.dto.request.CreateActivityRecordsRequest;
import depromeet.lessonfour.server.activityrecord.app.support.MealFoodFactory;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.domain.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.activityrecord.domain.vo.ActivityAt;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealFood;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class CreateActivityRecordUseCase {

  private final ActivityRecordRepository activityRecordRepository;
  private final MealFoodFactory mealFoodFactory;

  public void saveActivityRecord(Long userId, CreateActivityRecordsRequest dto) {
    List<MealFood> mealFoods = mealFoodFactory.createMealFoods(dto.foods());
    ActivityRecord activityRecord =
        ActivityRecord.createWithMeals(
            userId, dto.water(), dto.stress(), ActivityAt.from(dto.occurredAt()), mealFoods);

    try {
      activityRecordRepository.save(activityRecord);
    } catch (DataIntegrityViolationException e) {
      throw new ServerException(ErrorCode.CONFLICT);
    }
  }
}
