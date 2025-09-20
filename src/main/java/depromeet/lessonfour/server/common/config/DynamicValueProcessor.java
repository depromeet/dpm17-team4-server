package depromeet.lessonfour.server.common.config;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import depromeet.lessonfour.server.common.annotation.DynamicValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(2)  // SecretInitializer(Order=1) 이후에 실행
@RequiredArgsConstructor
@DependsOn("secretInitializer")
public class DynamicValueProcessor implements BeanPostProcessor {
    
    private final Environment environment;
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        
        for (Field field : clazz.getDeclaredFields()) {
            DynamicValue dynamicValue = field.getAnnotation(DynamicValue.class);
            if (dynamicValue != null) {
                processDynamicValue(bean, field, dynamicValue);
            }
        }
        
        return bean;
    }
    
    private void processDynamicValue(Object bean, Field field, DynamicValue dynamicValue) {
        try {
            String valueExpression = dynamicValue.value();
            String propertyKey = extractPropertyKey(valueExpression);
            String defaultValue = extractDefaultValue(valueExpression);
            
            // Secret Manager 키 자동 추론: kakao.client-id -> KAKAO__CLIENT_ID
            String secretKey = inferSecretKey(propertyKey);
            
            String value = null;
            String source = "";
            
            // 1. Secret Manager 값 우선 조회
            if (secretKey != null) {
                value = environment.getProperty(secretKey);
                if (value != null) {
                    source = "Secret Manager";
                }
            }
            
            // 2. Secret Manager 값이 없으면 application.yaml 값 사용
            if (value == null) {
                value = environment.getProperty(propertyKey, defaultValue);
                if (value != null && !value.equals(defaultValue)) {
                    source = "application.yaml";
                } else if (value != null) {
                    source = "default value";
                }
            }
            
            if (value != null) {
                field.setAccessible(true);
                field.set(bean, value);
            } else {
                log.warn("DynamicValue 값 없음: {} (키: {})", field.getName(), propertyKey);
            }
            
        } catch (Exception e) {
            log.warn("DynamicValue 처리 실패: {}", field.getName(), e);
        }
    }

    /**
     * ${key:default} 형태에서 key 추출
     */
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
    
    /**
     * ${key:default} 형태에서 default 값 추출
     */
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

    /**
     * Spring 설정 키를 Secret Manager 키로 자동 추론
     * 예: kakao.client-id -> KAKAO__CLIENT_ID
     */
    private String inferSecretKey(String propertyKey) {
        if (propertyKey == null || propertyKey.isEmpty()) {
            return null;
        }
        
        // kakao.client-id -> KAKAO__CLIENT_ID
        return propertyKey.toUpperCase()
                .replace(".", "__")
                .replace("-", "_");
    }
}
