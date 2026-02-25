package hello.anime_tier.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsBedrockService {

    private final BedrockRuntimeClient client;

    private static final Document TAG_TOOL_SCHEMA_DOC =
            Document.mapBuilder()
                    .putString("type", "object")
                    .putDocument("properties", Document.mapBuilder()
                            .putDocument("tag1", Document.mapBuilder().putString("type", "string").build())
                            .putDocument("tag2", Document.mapBuilder().putString("type", "string").build())
                            .putDocument("tag3", Document.mapBuilder().putString("type", "string").build())
                            .putDocument("tag4", Document.mapBuilder().putString("type", "string").build())
                            .putDocument("tag5", Document.mapBuilder().putString("type", "string").build())
                            .putDocument("primaryTag", Document.mapBuilder().putString("type", "string").build())
                            .build())
                    // required는 일단 빼도 되고, 넣고 싶으면 아래처럼 "array Document"로 넣어야 함
                    .putDocument("required", Document.listBuilder()
                            .addString("tag1")
                            .addString("tag2")
                            .addString("tag3")
                            .addString("tag4")
                            .addString("tag5")
                            .addString("primaryTag")
                            .build())
                    .build();

    public String askToAwsAiToolStructured(String userText) {
        try {
            ConverseResponse response = client.converse(req -> req
                    .modelId("amazon.nova-micro-v1:0")
                    .messages(m -> m.role(ConversationRole.USER)
                            .content(c -> c.text(userText)))
                    .inferenceConfig(ic -> ic.temperature(0f).maxTokens(400))
                    .toolConfig(tc -> tc.tools(t -> t.toolSpec(ts -> ts
                            .name("pick_tags")
                            .description("Pick 5 tags and 1 primaryTag from candidates.")
                            .inputSchema(s -> s.json(TAG_TOOL_SCHEMA_DOC))
                    )))
            );

            Message out = response.output().message();
            for (ContentBlock cb : out.content()) {
                if (cb.toolUse() != null) {
                    Document input = cb.toolUse().input();
                    return input.toString();
                }
            }
            return out.content().get(0).text();

        } catch (ValidationException e) {
            // ✅ 여기 메시지에 “어떤 필드가 invalid인지”가 들어있는 경우가 많음
            log.error("Bedrock ValidationException: msg='{}'", e.getMessage());
            // ✅ 요청ID 찍히면 AWS Support/CloudTrail에서 추적 쉬움
            if (e.awsErrorDetails() != null) {
                log.error("Bedrock errorDetails: service={}, errorCode={}, errorMessage={}",
                        e.awsErrorDetails().serviceName(),
                        e.awsErrorDetails().errorCode(),
                        e.awsErrorDetails().errorMessage());
            }
            log.error("Bedrock requestId={}", e.requestId());
            throw e;

        } catch (BedrockRuntimeException e) {
            // BedrockRuntime 쪽 다른 4xx/5xx도 여기에 잡힘
            log.error("BedrockRuntimeException: msg='{}', requestId={}", e.getMessage(), e.requestId());
            throw e;

        } catch (Exception e) {
            log.error("Unexpected exception while calling Bedrock: {}", e.toString(), e);
            throw e;
        }
    }

    public String askToAwsAi(String text) {
        try {
            ConverseResponse response = client.converse(req -> req
                    .modelId("amazon.nova-micro-v1:0")
                    .messages(m -> m.role(ConversationRole.USER)
                            .content(c -> c.text(text)))
            );
            return response.output().message().content().get(0).text();

        } catch (ValidationException e) {
            log.error("Bedrock ValidationException: msg='{}', requestId={}", e.getMessage(), e.requestId());
            throw e;

        } catch (BedrockRuntimeException e) {
            log.error("BedrockRuntimeException: msg='{}', requestId={}", e.getMessage(), e.requestId());
            throw e;
        }
    }
}