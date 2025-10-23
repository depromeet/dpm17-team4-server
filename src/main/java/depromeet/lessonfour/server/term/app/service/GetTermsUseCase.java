package depromeet.lessonfour.server.term.app.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import depromeet.lessonfour.server.common.annotation.UseCase;
import depromeet.lessonfour.server.term.api.dto.response.TermItemDto;
import depromeet.lessonfour.server.term.infra.TermResourceRepository;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTermsUseCase {

  private final TermResourceRepository repository;

  public List<TermItemDto> getAll() {
    return repository.loadAll();
  }
}
