package depromeet.lessonfour.server.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Secret Manager에서 동적으로 값을 가져오는 커스텀 어노테이션 @Value와 동일한 문법을 지원하지만 런타임에 최신 값을 조회
 *
 * <p>우선순위: 1. Secret Manager 값 (자동 추론) 2. application.yaml의 기본값
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
  /** 프로퍼티 키 (예: "${kakao.client-id}") @Value와 동일한 문법 사용 */
  String value();
}
