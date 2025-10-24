package depromeet.lessonfour.server.term.infra;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

import depromeet.lessonfour.server.term.api.dto.response.TermItemDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class TermResourceRepository {

  private static final String TERMS_GLOB = "classpath:/term/*.md";

  public List<TermItemDto> loadAll() {
    try {
      var resolver = new PathMatchingResourcePatternResolver();
      Resource[] resources = resolver.getResources(TERMS_GLOB);

      List<TermItemDto> result = new ArrayList<>();
      for (Resource r : resources) {
        String filename = r.getFilename() == null ? "" : r.getFilename();
        String content = r.getContentAsString(StandardCharsets.UTF_8);

        // 1) H1에서 타이틀 추출
        String title = extractH1Title(content);

        // 2) H1이 없으면 파일명으로 보조 매핑
        if (title == null) title = mapTitleByFilename(filename);

        // 3) 그래도 없으면 파일명(확장자 제거)
        if (title == null) title = filename.replaceFirst("\\.md$", "");

        result.add(new TermItemDto(title, content));
      }

      // 응답 순서 고정: 서비스이용약관 → 개인정보처리방침
      result.sort(Comparator.comparingInt(t -> rankByTitle(t.title())));
      return result;

    } catch (IOException e) {
      log.error("약관 리소스 로딩 실패: {}", e.getMessage(), e);
      return List.of();
    }
  }

  private static String extractH1Title(String md) {
    if (md == null) return null;
    return md.lines()
        .map(String::trim)
        .filter(l -> l.startsWith("# ")) // 첫 H1
        .findFirst()
        .map(l -> l.substring(2).trim())
        .orElse(null);
  }

  private static String mapTitleByFilename(String filename) {
    String f = filename.toLowerCase();
    if (f.startsWith("service-") || f.equals("service.md")) return "서비스이용약관";
    if (f.startsWith("privacy-") || f.equals("privacy.md")) return "개인정보처리방침";
    return null;
  }

  private static int rankByTitle(String title) {
    if ("서비스이용약관".equals(title)) return 0;
    if ("개인정보처리방침".equals(title)) return 1;
    return 9; // 기타는 뒤로
  }
}
