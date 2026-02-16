package hello.anime_tier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class AnimeTierApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnimeTierApplication.class, args);
    }

}
