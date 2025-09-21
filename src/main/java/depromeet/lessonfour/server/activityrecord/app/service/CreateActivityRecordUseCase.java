package depromeet.lessonfour.server.activityrecord.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.activityrecord.app.dto.request.CreateActivityRecordsRequest;
import depromeet.lessonfour.server.activityrecord.domain.entity.ActivityRecord;
import depromeet.lessonfour.server.activityrecord.domain.vo.MealFood;
import depromeet.lessonfour.server.activityrecord.infra.repository.ActivityRecordRepository;
import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.common.api.code.ErrorCode;
import depromeet.lessonfour.server.common.exception.ServerException;
import depromeet.lessonfour.server.user.app.service.UserQueryService;
import depromeet.lessonfour.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;

@UseCase
@Transactional
@RequiredArgsConstructor
public class CreateActivityRecordUseCase {

  private final UserQueryService userQueryService;
  private final ActivityRecordRepository activityRecordRepository;
  private final MealFoodFactory mealFoodFactory;

  public void saveActivityRecord(Long userId, CreateActivityRecordsRequest dto) {
    User user = userQueryService.findById(userId);
    validateNoDuplicateRecord(user, dto.occurredAt());

    List<MealFood> mealFoods = mealFoodFactory.createMealFoods(dto.foods());
    ActivityRecord activityRecord =
        ActivityRecord.createWithMeals(
            user, dto.water(), dto.stress(), dto.occurredAt(), mealFoods);

    activityRecordRepository.save(activityRecord);
  }

  private void validateNoDuplicateRecord(User user, LocalDateTime occurredAt) {
    LocalDate date = occurredAt.toLocalDate();
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

    if (activityRecordRepository.existsByUser_IdAndActivityAtBetweenAndIsDeletedFalse(
        user.getId(), startOfDay, endOfDay)) {
      throw new ServerException(ErrorCode.CONFLICT);
    }
  }
}
