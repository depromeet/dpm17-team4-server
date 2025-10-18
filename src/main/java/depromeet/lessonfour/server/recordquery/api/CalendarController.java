package depromeet.lessonfour.server.recordquery.api;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.api.code.SuccessCode;
import depromeet.lessonfour.server.common.api.dto.SuccessResponse;
import depromeet.lessonfour.server.recordquery.app.dto.RecordExistenceListResponse;
import depromeet.lessonfour.server.recordquery.app.dto.ToiletTimeListResponse;
import depromeet.lessonfour.server.recordquery.app.service.GetRecordUseCase;
import depromeet.lessonfour.server.recordquery.app.service.GetToiletTimesUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Valid
@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class CalendarController {

  private final GetRecordUseCase getRecordUseCase;
  private final GetToiletTimesUseCase getToiletTimesUseCase;

  @GetMapping
  public SuccessResponse<RecordExistenceListResponse> getSingleRecord(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @NotNull(message = "날짜는 필수입니다") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate start,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate end) {

    return SuccessResponse.of(
        SuccessCode.SUCCESS_FETCH, getRecordUseCase.getRecordExistenceList(userId, start, end));
  }

  @GetMapping("/poo-records")
  public SuccessResponse<ToiletTimeListResponse> getToiletTimesByDate(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @NotNull(message = "date는 필수입니다") @RequestParam("date")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date) {

    return SuccessResponse.of(
        SuccessCode.SUCCESS_FETCH, getToiletTimesUseCase.getToiletTimes(userId, date));
  }
}
