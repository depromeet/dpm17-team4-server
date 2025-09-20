package depromeet.lessonfour.server.common.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(1)  // 가장 높은 우선순위
public class SecretInitializer {
    
    @Autowired
    private SecretManager secretManager;
    
    @Autowired
    private ConfigurableEnvironment environment;
    
    @PostConstruct
    public void initializeSecrets() {
        try {
            log.info("Secret Manager에서 시크릿 정보를 불러오는 중...");
            Map<String, Object> secrets = secretManager.getSecretValues();
            if (!secrets.isEmpty()) {
                // Spring Environment에 시크릿 프로퍼티 추가 (최우선순위)
                MapPropertySource secretPropertySource = new MapPropertySource("secretManager", secrets);
                environment.getPropertySources().addFirst(secretPropertySource);
                log.info("Secret Manager에서 {}개의 시크릿을 성공적으로 로드했습니다.", secrets.size());
            } else {
                log.warn("Secret Manager에서 시크릿을 가져오지 못했습니다. 기본 설정값을 사용합니다.");
            }
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.warn("Secret Manager 인증 실패. 기본 설정값을 사용합니다.", e);
        } catch (Exception e) {
            log.warn("시크릿 초기화 중 오류 발생. 기본 설정값을 사용합니다.", e);
        }
    }
}
