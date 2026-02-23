package hello.anime_tier.domain.search.service;

import hello.anime_tier.dto.TagSearchResponseDto;
import hello.anime_tier.entity.TagEntity;
import hello.anime_tier.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TagSearchService {

    private final TagRepository tagRepository;
    private final TransformersEmbeddingModel embeddingModel;
    private final AwsBedrockService awsBedrockService;
    private final TextSplitter textSplitter;

    public TagSearchService(TagRepository tagRepository,
                            TransformersEmbeddingModel embeddingModel,
                            AwsBedrockService awsBedrockService,
                            @Qualifier("sentenceSplitService") TextSplitter textSplitter) {
        this.tagRepository = tagRepository;
        this.embeddingModel = embeddingModel;
        this.awsBedrockService = awsBedrockService;
        this.textSplitter = textSplitter;
    }

    public TagSearchResponseDto extractTopFinalTags(String userInput){
        List<String> candidateTags = extractTopTags(userInput);

        String tagsString = String.join(", ", candidateTags);

        String aiRequest = String.format(
                "당신은 애니메이션 태그 전문가입니다. 아래 제공된 태그 리스트에서 사용자의 질문 의도를 가장 잘 반영하는 태그를 선정하세요.\n" +
                        "\n" +
                        "[제공된 태그 리스트]: %s\n" +
                        "[사용자의 질문]: %s\n" +
                        "\n" +
                        "[작업 지침]:\n" +
                        "1. 제공된 리스트에 있는 태그만 사용하세요.\n" +
                        "2. 질문의 '핵심 주제'나 '장르'를 가장 잘 나타내는 태그 5개를 선정하세요.\n" +
                        "3. 선정된 7개 중, 검색 결과에서 가장 우선순위가 높은 핵심 태그 1개를 반드시 선택하세요.\n" +
                        "\n" +
                        "[출력 형식]:\n" +
                        "반드시 아래 형식만 출력하세요. 다른 설명은 생략합니다:\n" +
                        "{태그1}, {태그2}, {태그3}, {태그4}, {태그5}\n" +
                        "가장 중요한 태그: [태그명]",
                tagsString, userInput
        );

        String response = awsBedrockService.askToAwsAi(aiRequest);
        
        log.info("⭐⭐⭐⭐⭐Bedrock raw response: {}", response);
        
        List<String> finalTags = divideResponse(response);
        String priorityTag = dividePriorityResponse(response);

        // priorityTag 보정: finalTags에 반드시 포함
        if (priorityTag != null && !priorityTag.isBlank() && !finalTags.contains(priorityTag)) {
            // priorityTag가 candidateTags 밖에서 튀어나오는 경우도 있으니(가끔) 추가 가능
            finalTags = new ArrayList<>(finalTags); // toList()는 불변일 수 있어서 방어
            finalTags.add(0, priorityTag);
        }

        return new TagSearchResponseDto(candidateTags, finalTags, priorityTag);
    }

    private List<String> divideResponse(String response){
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(response);

        while (matcher.find()) {
            result.add(matcher.group(1));
        }

        return result;
    }

    private String dividePriorityResponse(String response){
        String result = "";
        Pattern bracketPattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = bracketPattern.matcher(response);
        if(matcher.find()){
            result = matcher.group(1).trim();
        }
        return result;
    }

    private List<String> extractTopTags(String userInput) {
        List<String> sentences = textSplitter.split(userInput);

        return sentences.stream()
                .flatMap(sentence -> {
                    float[] sentenceVector = embeddingModel.embed(sentence);
                    String vec = toPgVectorLiteral(sentenceVector);

                    // ✅ DB에서 topK만 가져오기
                    return tagRepository.findTopKTagNamesByVector(vec, 20).stream();
                })
                .distinct()
                .collect(Collectors.toList());
    }


    private String toPgVectorLiteral(float[] v) {
        // pgvector 입력 형식: [1,2,3] 또는 [1.0,2.0,...]
        StringBuilder sb = new StringBuilder(v.length * 8);
        sb.append('[');
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(',');
            // 너무 긴 소수는 불필요. 성능/문자열 길이 줄이기 위해 적당히 제한
            sb.append(String.format(java.util.Locale.US, "%.6f", v[i]));
        }
        sb.append(']');
        return sb.toString();
    }

    private double calculateCosineSimilarity(float[] v1, float[] v2) {
        if (v1.length != v2.length) return 0.0;
        double dotProduct = 0, normA = 0, normB = 0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += Math.pow(v1[i], 2);
            normB += Math.pow(v2[i], 2);
        }
        return (normA == 0 || normB == 0) ? 0.0 : dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record TagVector(String tagName, float[] vector) {}
    private record TagScore(String tagName, double score) {}
}