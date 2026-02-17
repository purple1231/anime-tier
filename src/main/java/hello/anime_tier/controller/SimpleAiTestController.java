package hello.anime_tier.controller;

import hello.anime_tier.service.AwsBedrockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SimpleAiTestController {

    private final AwsBedrockService awsBedrockService;

    @PostMapping("/ai/test")
    public String test(@RequestBody String prompt) {
        try {
            return awsBedrockService.askToAwsAi(prompt);
        } catch (Exception e) {
            return "에러: " + e.getMessage();
        }
    }
}