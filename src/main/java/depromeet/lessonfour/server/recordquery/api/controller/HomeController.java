package depromeet.lessonfour.server.recordquery.api.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.api.code.SuccessCode;
import depromeet.lessonfour.server.common.api.dto.SuccessResponse;
import depromeet.lessonfour.server.recordquery.app.dto.HomeResponseDto;
import depromeet.lessonfour.server.recordquery.app.service.GetDailyRecordUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Validated
@Tag(name = "홈", description = "홈 관련 API 문서입니다.")
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

  private final GetDailyRecordUseCase getDailyRecordUseCase;

  @Operation(summary = "홈 데이터 조회", description = "특정 날짜의 홈 화면 정보를 조회합니다.")
  @GetMapping("/{date}")
  public SuccessResponse<HomeResponseDto> getHome(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return SuccessResponse.of(
        SuccessCode.SUCCESS_FETCH, getDailyRecordUseCase.getHomeRecord(userId, date));
  }
}
