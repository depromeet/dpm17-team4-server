package depromeet.lessonfour.server.activityrecord.app.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.activityrecord.app.dto.request.CreateActivityRecordsRequest;
import depromeet.lessonfour.server.activityrecord.app.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.activityrecord.app.support.MealFoodFactory;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.domain.service.ActivityRecordCreationPolicy;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealFood;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.user.app.service.UserQueryService;
import depromeet.lessonfour.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class CreateActivityRecordUseCase {

  private final UserQueryService userQueryService;
  private final ActivityRecordRepository activityRecordRepository;
  private final ActivityRecordCreationPolicy activityRecordCreationPolicy;
  private final MealFoodFactory mealFoodFactory;

  public void saveActivityRecord(Long userId, CreateActivityRecordsRequest dto) {
    User user = userQueryService.getActivatedUserById(userId);
    activityRecordCreationPolicy.validateNoDuplicateRecord(user, dto.occurredAt());

    List<MealFood> mealFoods = mealFoodFactory.createMealFoods(dto.foods());
    ActivityRecord activityRecord =
        ActivityRecord.createWithMeals(
            user, dto.water(), dto.stress(), dto.occurredAt(), mealFoods);

    activityRecordRepository.save(activityRecord);
  }
}
