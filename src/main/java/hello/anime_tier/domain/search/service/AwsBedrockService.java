package hello.anime_tier.domain.search.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;

@Service
@RequiredArgsConstructor
public class AwsBedrockService {


    private final BedrockRuntimeClient client;

    public String askToAwsAi(String text){
        ConverseResponse response = client.converse(request -> request.modelId("amazon.nova-micro-v1:0")
                .messages(
                        t -> t.role("user").content(c->c.text(text)
                        )
                )
        );
        return response.output().message().content().get(0).text();
    }



}
