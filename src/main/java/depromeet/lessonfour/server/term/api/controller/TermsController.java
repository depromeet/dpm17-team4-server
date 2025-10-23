package depromeet.lessonfour.server.term.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import depromeet.lessonfour.server.common.api.code.SuccessCode;
import depromeet.lessonfour.server.common.api.dto.SuccessResponse;
import depromeet.lessonfour.server.term.api.dto.response.TermItemDto;
import depromeet.lessonfour.server.term.app.service.GetTermsUseCase;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/terms")
public class TermsController {

  private final GetTermsUseCase getTermsUseCase;

  @GetMapping
  public SuccessResponse<List<TermItemDto>> list() {
    return SuccessResponse.of(SuccessCode.SUCCESS_FETCH, getTermsUseCase.getAll());
  }
}
