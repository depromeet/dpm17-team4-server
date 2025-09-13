package depromeet.lessonfour.server.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import depromeet.lessonfour.server.common.converter.StringToEnumCustomConverterFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	// 쉼표 구분 문자열을 배열로 쓰기 위해 SpEL로 split (따옴표/공백 안전)
	@Value("#{'${cors.allowed-origins}'.split('\\s*,\\s*')}")
	private String[] allowedOrigins;

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverterFactory(new StringToEnumCustomConverterFactory());
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins(allowedOrigins)
			.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
			.allowedHeaders("*")
			.allowCredentials(true);
	}
}
