package hello.anime_tier.domain.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.anime_tier.dto.BedrockToolPick;
import hello.anime_tier.dto.TagSearchResponseDto;
import hello.anime_tier.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TagSearchService {

    private final TagRepository tagRepository;
    private final TransformersEmbeddingModel embeddingModel;
    private final AwsBedrockService awsBedrockService;
    private final TextSplitter textSplitter;
    private final ObjectMapper objectMapper;

    public TagSearchService(TagRepository tagRepository,
                            TransformersEmbeddingModel embeddingModel,
                            AwsBedrockService awsBedrockService,
                            @Qualifier("sentenceSplitService") TextSplitter textSplitter,
                            ObjectMapper objectMapper) {
        this.tagRepository = tagRepository;
        this.embeddingModel = embeddingModel;
        this.awsBedrockService = awsBedrockService;
        this.textSplitter = textSplitter;
        this.objectMapper = objectMapper;
    }

    public TagSearchResponseDto extractTopFinalTags(String userInput) {
        List<String> candidateTags = extractTopTags(userInput);
        Set<String> candidateSet = new HashSet<>(candidateTags);

        String userText = buildUserText(candidateTags, userInput);

        BedrockToolPick pick = callParseValidateWithRetry(userText, candidateSet);

        List<String> finalTags = toolPickToTags(pick);   // ✅ 여기서 tag1~tag5 -> List
        String priorityTag = pick.primaryTag();

        // priorityTag 보정: finalTags에 반드시 포함
        if (priorityTag != null && !priorityTag.isBlank() && !finalTags.contains(priorityTag)) {
            finalTags.add(0, priorityTag);
        }

        // 혹시 중복/개수 문제 생기면 정리(안전장치)
        finalTags = finalTags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .limit(5)
                .collect(Collectors.toCollection(ArrayList::new));

        if (priorityTag != null && !priorityTag.isBlank() && !finalTags.contains(priorityTag)) {
            finalTags.add(0, priorityTag);
            if (finalTags.size() > 5) finalTags = finalTags.subList(0, 5);
        }

        return new TagSearchResponseDto(candidateTags, finalTags, priorityTag);
    }

    private String buildUserText(List<String> candidateTags, String userInput) {
        return """
        [candidate_tags]
        %s

        [user_query]
        %s

        [rules]
        - tag1~tag5는 candidate_tags 안에서만 선택
        - tag1~tag5는 서로 달라야 함(중복 금지)
        - primaryTag는 tag1~tag5 중 하나
        """.formatted(String.join(", ", candidateTags), userInput);
    }

    private BedrockToolPick callParseValidateWithRetry(String userText, Set<String> candidateSet) {
        // 1차
        BedrockToolPick pick = callOnce(userText);
        if (isValid(pick, candidateSet)) return normalize(pick);

        // 2차 (1회 재시도)
        String retryText = userText + """

        [retry_note]
        이전 응답이 규칙을 위반했습니다.
        반드시 candidate_tags 안에서만 선택하고, tag1~tag5는 서로 다른 값으로 채우고,
        primaryTag는 tag1~tag5 중 하나로 설정하세요.
        """;

        BedrockToolPick pick2 = callOnce(retryText);
        if (isValid(pick2, candidateSet)) return normalize(pick2);

        // fallback: 후보 태그 상위 5개로 (6개 인자 맞춰 생성)
        List<String> fallback = candidateSet.stream().limit(5).toList();
        String t1 = fallback.size() > 0 ? fallback.get(0) : "";
        String t2 = fallback.size() > 1 ? fallback.get(1) : t1;
        String t3 = fallback.size() > 2 ? fallback.get(2) : t1;
        String t4 = fallback.size() > 3 ? fallback.get(3) : t1;
        String t5 = fallback.size() > 4 ? fallback.get(4) : t1;
        String primary = !t1.isBlank() ? t1 : null;

        return new BedrockToolPick(t1, t2, t3, t4, t5, primary);
    }

    private BedrockToolPick callOnce(String userText) {
        String raw = awsBedrockService.askToAwsAiToolStructured(userText);
        log.info("⭐⭐⭐⭐⭐Bedrock raw response: {}", raw);

        try {
            return objectMapper.readValue(raw, BedrockToolPick.class);
        } catch (Exception e) {
            log.warn("Bedrock JSON parse failed: {}", e.getMessage());
            return null;
        }
    }

    private boolean isValid(BedrockToolPick pick, Set<String> candidateSet) {
        if (pick == null) return false;

        List<String> tags = toolPickToTags(pick);
        if (tags.size() != 5) return false;

        String primary = pick.primaryTag();
        if (primary == null || primary.isBlank()) return false;

        // primary는 5개 중 하나여야 함
        if (!tags.contains(primary.trim())) return false;

        // 전부 후보 안에 있어야 함 + 공백/널 금지
        for (String t : tags) {
            if (t == null || t.isBlank()) return false;
            if (!candidateSet.contains(t.trim())) return false;
        }

        // 중복 금지 (5개가 모두 달라야)
        long distinctCount = tags.stream().map(String::trim).distinct().count();
        return distinctCount == 5;
    }

    private BedrockToolPick normalize(BedrockToolPick pick) {
        // trim 정리
        String t1 = safeTrim(pick.tag1());
        String t2 = safeTrim(pick.tag2());
        String t3 = safeTrim(pick.tag3());
        String t4 = safeTrim(pick.tag4());
        String t5 = safeTrim(pick.tag5());
        String primary = safeTrim(pick.primaryTag());

        // 혹시 중복이 섞여오면(가끔) 순서 유지하며 중복 제거 후 다시 5개 채우기
        LinkedHashSet<String> uniq = new LinkedHashSet<>();
        for (String t : List.of(t1, t2, t3, t4, t5)) {
            if (!t.isBlank()) uniq.add(t);
        }

        List<String> tags = new ArrayList<>(uniq);
        while (tags.size() < 5) { // 부족하면 앞에서 반복 채움(안전장치)
            tags.add(tags.isEmpty() ? "" : tags.get(0));
        }
        if (tags.size() > 5) tags = tags.subList(0, 5);

        // primary가 tags 밖이면 tags[0]로 보정
        if (primary.isBlank() || !tags.contains(primary)) {
            primary = tags.get(0);
        }

        return new BedrockToolPick(tags.get(0), tags.get(1), tags.get(2), tags.get(3), tags.get(4), primary);
    }

    private List<String> toolPickToTags(BedrockToolPick pick) {
        if (pick == null) return List.of();
        return new ArrayList<>(List.of(
                safeTrim(pick.tag1()),
                safeTrim(pick.tag2()),
                safeTrim(pick.tag3()),
                safeTrim(pick.tag4()),
                safeTrim(pick.tag5())
        ));
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    // 문장 자르기 -> 각 문장당 임베딩 및 각 20개 가져오기
    private List<String> extractTopTags(String userInput) {
        List<String> sentences = textSplitter.split(userInput);

        return sentences.stream()
                .flatMap(sentence -> {
                    float[] sentenceVector = embeddingModel.embed(sentence);
                    String vec = toPgVectorLiteral(sentenceVector);
                    return tagRepository.findTopKTagNamesByVector(vec, 20).stream();
                })
                .distinct()
                .collect(Collectors.toList());
    }

    private String toPgVectorLiteral(float[] v) {
        StringBuilder sb = new StringBuilder(v.length * 8);
        sb.append('[');
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(String.format(java.util.Locale.US, "%.6f", v[i]));
        }
        sb.append(']');
        return sb.toString();
    }
}