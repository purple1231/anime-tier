package hello.anime_tier.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration // "이 클래스는 설정 파일이야!"
public class AppConfig {

    @Bean // "스프링아, 내가 RestTemplate 객체를 만들었으니 네가 관리해줘!"
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}