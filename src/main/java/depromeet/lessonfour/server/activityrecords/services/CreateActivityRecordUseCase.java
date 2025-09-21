package depromeet.lessonfour.server.activityrecords.services;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.activityrecords.adapters.ActivityRecordRepository;
import depromeet.lessonfour.server.activityrecords.domain.entities.ActivityRecord;
import depromeet.lessonfour.server.activityrecords.domain.vo.MealFood;
import depromeet.lessonfour.server.activityrecords.schemas.request.CreateActivityRecordsRequest;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.common.exception.code.ErrorCode;
import depromeet.lessonfour.server.users.domain.entities.User;
import depromeet.lessonfour.server.users.services.UserQueryService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateActivityRecordUseCase {

  private final UserQueryService userQueryService;
  private final ActivityRecordRepository activityRecordRepository;
  private final MealFoodFactory mealFoodFactory;
  private final Clock clock;

  public void saveActivityRecord(Long userId, CreateActivityRecordsRequest dto) {
    User user = userQueryService.findById(userId);
    validateNoDuplicateRecord(user);

    List<MealFood> mealFoods = mealFoodFactory.createMealFoods(dto.foods());
    ActivityRecord activityRecord =
        ActivityRecord.createWithMeals(
            user, dto.water(), dto.stress(), dto.occurredAt(), mealFoods);

    activityRecordRepository.save(activityRecord);
  }

  private void validateNoDuplicateRecord(User user) {
    LocalDate today = LocalDate.now(clock);
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

    if (activityRecordRepository.existsByUser_IdAndCreatedAtBetween(
        user.getId(), startOfDay, endOfDay)) {
      throw new ServerException(ErrorCode.CONFLICT);
    }
  }
}
