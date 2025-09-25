package depromeet.lessonfour.server.toiletrecord.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.api.code.SuccessCode;
import depromeet.lessonfour.server.common.api.dto.SuccessResponse;
import depromeet.lessonfour.server.toiletrecord.app.dto.request.ToiletRecordCreateRequestDto;
import depromeet.lessonfour.server.toiletrecord.app.dto.request.ToiletRecordUpdateRequestDto;
import depromeet.lessonfour.server.toiletrecord.app.dto.response.ToiletRecordResponseDto;
import depromeet.lessonfour.server.toiletrecord.app.service.CreateToiletRecordUseCase;
import depromeet.lessonfour.server.toiletrecord.app.service.DeleteToiletRecordUseCase;
import depromeet.lessonfour.server.toiletrecord.app.service.QueryToiletRecordUseCase;
import depromeet.lessonfour.server.toiletrecord.app.service.UpdateToiletRecordUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/poo-records")
public class ToiletRecordController {

  private final CreateToiletRecordUseCase createUseCase;
  private final UpdateToiletRecordUseCase updateUseCase;
  private final DeleteToiletRecordUseCase deleteUseCase;
  private final QueryToiletRecordUseCase queryUseCase;

  @Operation(summary = "배변기록 등록")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "success"),
    @ApiResponse(responseCode = "500", description = "server error", content = @Content)
  })
  @PostMapping
  public ResponseEntity<SuccessResponse<ToiletRecordResponseDto>> createToiletRecord(
      @Valid @RequestBody ToiletRecordCreateRequestDto request,
      @AuthenticationPrincipal(expression = "id") Long userId) {
    return ResponseEntity.ok(
        SuccessResponse.of(SuccessCode.SUCCESS_CREATE, createUseCase.create(userId, request)));
  }

  @Operation(summary = "배변기록 상세조회")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "success"),
    @ApiResponse(responseCode = "404", description = "not found"),
    @ApiResponse(responseCode = "500", description = "server error", content = @Content)
  })
  @GetMapping("/{toiletRecordId}/detail")
  public ResponseEntity<SuccessResponse<ToiletRecordResponseDto>> getToiletRecordDetail(
      @PathVariable Long toiletRecordId, @AuthenticationPrincipal(expression = "id") Long userId) {
    return ResponseEntity.ok(
        SuccessResponse.of(
            SuccessCode.SUCCESS_FETCH, queryUseCase.getDetail(userId, toiletRecordId)));
  }

  @Operation(summary = "배변기록 수정")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "success"),
    @ApiResponse(responseCode = "404", description = "not found"),
    @ApiResponse(responseCode = "500", description = "server error", content = @Content)
  })
  @PatchMapping("/{toiletRecordId}")
  public ResponseEntity<SuccessResponse<ToiletRecordResponseDto>> updateToiletRecord(
      @Valid @RequestBody ToiletRecordUpdateRequestDto request,
      @PathVariable Long toiletRecordId,
      @AuthenticationPrincipal(expression = "id") Long userId) {
    return ResponseEntity.ok(
        SuccessResponse.of(
            SuccessCode.SUCCESS_UPDATE, updateUseCase.update(userId, toiletRecordId, request)));
  }

  @Operation(summary = "배변기록 삭제")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "success"),
    @ApiResponse(responseCode = "404", description = "not found"),
    @ApiResponse(responseCode = "500", description = "server error", content = @Content)
  })
  @DeleteMapping("/{toiletRecordId}")
  public ResponseEntity<SuccessResponse<Void>> deleteToiletRecord(
      @PathVariable Long toiletRecordId, @AuthenticationPrincipal(expression = "id") Long userId) {
    deleteUseCase.delete(userId, toiletRecordId);
    return ResponseEntity.ok(SuccessResponse.of(SuccessCode.SUCCESS_DELETE));
  }
}
