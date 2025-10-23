package depromeet.lessonfour.server.recordquery.api.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.api.code.SuccessCode;
import depromeet.lessonfour.server.common.api.dto.SuccessResponse;
import depromeet.lessonfour.server.recordquery.app.dto.DailyRecordResponse;
import depromeet.lessonfour.server.recordquery.app.dto.RecordExistenceListResponse;
import depromeet.lessonfour.server.recordquery.app.service.GetDailyExistencesUseCase;
import depromeet.lessonfour.server.recordquery.app.service.GetDailyRecordUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Validated
@Tag(name = "캘린더", description = "캘린더 관련 API 문서입니다.")
@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class CalendarController {

  private final GetDailyExistencesUseCase getDailyExistencesUseCase;
  private final GetDailyRecordUseCase getDailyRecordUseCase;

  @Operation(summary = "기록 존재 여부 조회", description = "특정 기간 동안의 활동 및 배변 기록 존재 여부를 조회합니다.")
  @GetMapping
  public SuccessResponse<RecordExistenceListResponse> getDailyExistences(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @NotNull(message = "시작 날짜는 필수입니다.") @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate start,
      @NotNull(message = "종료 날짜는 필수입니다.") @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate end) {

    return SuccessResponse.of(
        SuccessCode.SUCCESS_FETCH,
        getDailyExistencesUseCase.getRecordExistenceList(userId, start, end));
  }

  @Operation(summary = "일일 기록 조회", description = "특정 날짜의 활동 및 배변 기록을 조회합니다.")
  @GetMapping("/{date}")
  public SuccessResponse<DailyRecordResponse> getDailyRecord(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return SuccessResponse.of(
        SuccessCode.SUCCESS_FETCH, getDailyRecordUseCase.getDailyRecord(userId, date));
  }
}
