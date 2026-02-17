package hello.anime_tier.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration // "이 클래스는 설정 파일이야!"
public class AppConfig {

    @Bean // "스프링아, 내가 RestTemplate 객체를 만들었으니 네가 관리해줘!"
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient(){
        // 인증 정보를 환경변수에서 가져와서 '공용 클라이언트'를 딱 하나 만듭니다.
        return BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                System.getenv("AWS_ACCESS_KEY_ID"),
                                System.getenv("AWS_SECRET_ACCESS_KEY")
                        )
                )).build();
    }
}