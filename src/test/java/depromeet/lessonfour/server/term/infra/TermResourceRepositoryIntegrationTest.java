package depromeet.lessonfour.server.term.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import depromeet.lessonfour.server.term.api.dto.response.TermItemDto;

@SpringBootTest
@ActiveProfiles("test")
class TermResourceRepositoryIntegrationTest {

  @Autowired private TermResourceRepository repository;

  @Test
  @DisplayName("리소스 폴더(/term/*.md)에서 약관 md를 읽어 타이틀/본문을 반환하고, 약관→개인정보 순으로 정렬한다")
  void loadAll_termsFromResources_returnsSortedDtos() {
    // when
    List<TermItemDto> items = repository.loadAll();

    // then
    assertThat(items).isNotNull();
    // 최소 2개(서비스이용약관, 개인정보처리방침)
    assertThat(items.size()).isGreaterThanOrEqualTo(2);

    // 첫 두 개가 기대 순서인지(서비스이용약관 → 개인정보처리방침)
    assertThat(items.get(0).termTitle()).isEqualTo("서비스이용약관");
    assertThat(items.get(1).termTitle()).isEqualTo("개인정보처리방침");

    // 타이틀 셋 포함 여부
    assertThat(items.stream().map(TermItemDto::termTitle)).contains("서비스이용약관", "개인정보처리방침");

    // 본문은 비어있지 않고 개행이 \n 기준으로 들어오는지(정규화 확인)
    assertThat(items.get(0).termContent()).isNotBlank();
    assertThat(items.get(1).termContent()).isNotBlank();
    assertThat(items.get(0).termContent()).doesNotContain("\r\n");
    assertThat(items.get(1).termContent()).doesNotContain("\r\n");

    // H1 타이틀 추출 또는 파일명 매핑이 정상 동작해서 한글 타이틀로 들어왔는지
    assertThat(items.get(0).termTitle()).isIn("서비스이용약관", "개인정보처리방침");
    assertThat(items.get(1).termTitle()).isIn("서비스이용약관", "개인정보처리방침");
  }
}
