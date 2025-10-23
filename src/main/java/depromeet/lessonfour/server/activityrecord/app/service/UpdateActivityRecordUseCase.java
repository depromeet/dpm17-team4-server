package depromeet.lessonfour.server.activityrecord.app.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.activityrecord.app.dto.request.ActivityRecordDto;
import depromeet.lessonfour.server.activityrecord.app.support.MealFoodFactory;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.domain.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealFood;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class UpdateActivityRecordUseCase {

  private final ActivityRecordRepository activityRecordRepository;
  private final MealFoodFactory mealFoodFactory;

  public void updateActivityRecord(Long userId, Long activityRecordId, ActivityRecordDto dto) {
    ActivityRecord activityRecord =
        activityRecordRepository
            .findById(activityRecordId)
            .orElseThrow(() -> new ServerException(ErrorCode.DATA_NOT_FOUND));

    if (!activityRecord.isOwnedBy(userId)) {
      throw new ServerException(ErrorCode.ACCESS_DENIED);
    }

    List<MealFood> mealFoods = mealFoodFactory.createMealFoods(dto.foods());

    activityRecord.update(dto.water(), dto.stress(), mealFoods);
  }
}
