package depromeet.lessonfour.server.common.utils;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("valueProcessor")
@Order(2) // SecretInitializer(Order=1) 이후에 실행
@RequiredArgsConstructor
@DependsOn("secretInitializer")
public class ValueProcessor implements BeanPostProcessor {

  private final Environment environment;

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> clazz = bean.getClass();

    for (Field field : clazz.getDeclaredFields()) {
      Value value = field.getAnnotation(Value.class);
      if (value != null) {
        processValue(bean, field, value);
      }
    }

    return bean;
  }

  private void processValue(Object bean, Field field, Value value) {
    try {
      String valueExpression = value.value();
      String propertyKey = extractPropertyKey(valueExpression);
      String defaultValue = extractDefaultValue(valueExpression);

      log.debug("Value 처리 시작: {} = {}", field.getName(), valueExpression);
      log.debug("추출된 propertyKey: {}, defaultValue: {}", propertyKey, defaultValue);

      // Secret Manager 키 자동 추론: jwt.secret -> JWT__SECRET
      String secretKey = inferSecretKey(propertyKey);
      log.debug("추론된 secretKey: {}", secretKey);

      String valueToSet = null;
      String source = "";

      if (secretKey != null) {
        valueToSet = environment.getProperty(secretKey);
        log.debug(
            "Secret Manager 키 '{}' 조회 결과: {}", secretKey, valueToSet != null ? "값 있음" : "값 없음");
        if (valueToSet != null) {
          source = "Secret Manager";
        }
      }

      if (valueToSet == null) {
        valueToSet = environment.getProperty(propertyKey, defaultValue);
        log.debug(
            "Application 키 '{}' 조회 결과: {}", propertyKey, valueToSet != null ? "값 있음" : "값 없음");
        if (valueToSet != null) {
          source = "application.yaml";
        }
      }

      if (valueToSet != null) {
        field.setAccessible(true);
        Object convertedValue = convertValue(valueToSet, field.getType());
        field.set(bean, convertedValue);
        log.debug("Value 주입 성공: {} = {} (from {})", field.getName(), maskValue(valueToSet), source);
      } else {
        log.error("Value 값 없음: {} (키: {})", field.getName(), propertyKey);
      }

    } catch (Exception e) {
      log.error("Value 처리 실패: {}", field.getName(), e);
    }
  }

  /** ${key:default} 형태에서 key 추출 */
  private String extractPropertyKey(String valueExpression) {
    if (valueExpression.startsWith("${") && valueExpression.endsWith("}")) {
      String content = valueExpression.substring(2, valueExpression.length() - 1);
      // :default 부분 제거
      int colonIndex = content.indexOf(':');
      if (colonIndex != -1) {
        return content.substring(0, colonIndex);
      }
      return content;
    }
    return valueExpression;
  }

  /** ${key:default} 형태에서 default 값 추출 */
  private String extractDefaultValue(String valueExpression) {
    if (valueExpression.startsWith("${") && valueExpression.endsWith("}")) {
      String content = valueExpression.substring(2, valueExpression.length() - 1);
      int colonIndex = content.indexOf(':');
      if (colonIndex != -1) {
        return content.substring(colonIndex + 1);
      }
    }
    return "";
  }

  /** Spring 설정 키를 Secret Manager 키로 자동 추론 예: kakao.client-id -> KAKAO__CLIENT_ID */
  private String inferSecretKey(String propertyKey) {
    if (propertyKey == null || propertyKey.isEmpty()) {
      return null;
    }

    // jwt.secret -> JWT__SECRET
    return propertyKey.toUpperCase().replace(".", "__").replace("-", "_");
  }

  private String maskValue(String value) {
    if (value == null || value.length() <= 4) {
      return "***";
    }
    return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
  }

  private Object convertValue(String value, Class<?> targetType) {
    if (targetType == String.class) {
      return value;
    } else if (targetType == Integer.class || targetType == int.class) {
      return Integer.valueOf(value);
    } else if (targetType == Long.class || targetType == long.class) {
      return Long.valueOf(value);
    } else if (targetType == Boolean.class || targetType == boolean.class) {
      return Boolean.valueOf(value);
    } else if (targetType == Double.class || targetType == double.class) {
      return Double.valueOf(value);
    } else if (targetType == Float.class || targetType == float.class) {
      return Float.valueOf(value);
    } else {
      log.warn("지원하지 않는 타입: {}", targetType);
      return value;
    }
  }
}
