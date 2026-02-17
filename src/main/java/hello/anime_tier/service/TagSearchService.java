package hello.anime_tier.service;

import hello.anime_tier.entity.TagEntity;
import hello.anime_tier.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagSearchService {

    private final TagRepository tagRepository;
    private final TransformersEmbeddingModel embeddingModel;

    public List<String> extractTopTags(String userInput) {
        // 1. 문장 분리
        List<String> sentences = splitIntoSentences(userInput);

        // 2. 태그 리스트 로드
        List<TagEntity> allTags = tagRepository.findAll();

        // 최적화: 태그 벡터를 float로 미리 변환 (루프 밖에서 처리)
        List<TagVector> preparedTags = allTags.stream()
                .filter(tag -> tag.getTagEmbedding() != null)
                .map(tag -> new TagVector(tag.getTagName(), convertToFloatArray(tag.getTagEmbedding())))
                .toList();

        // 3. 문단(문장)별로 처리
        return sentences.stream()
                .flatMap(sentence -> {
                    // 각 문장을 임베딩
                    float[] sentenceVector = embeddingModel.embed(sentence);

                    // 해당 문장에서 가장 유사도가 높은 태그 2개 추출
                    return preparedTags.stream()
                            .map(tv -> new TagScore(
                                    tv.tagName(),
                                    calculateCosineSimilarity(sentenceVector, tv.vector())
                            ))
                            .sorted(Comparator.comparingDouble(TagScore::score).reversed())
                            .limit(2); // ✨ 문단별로 상위 2개만 먼저 선정
                })
                // 4. 모든 문단에서 모인 태그들을 하나로 합치고 중복 제거
                .map(TagScore::tagName)
                .distinct() // 중복된 태그(예: 여러 문단에서 'Action'이 나온 경우) 제거
                .collect(Collectors.toList());
    }

    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.ENGLISH);
        boundary.setText(text);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            String sentence = text.substring(start, end).trim();
            if (!sentence.isEmpty()) sentences.add(sentence);
        }
        return sentences;
    }

    private float[] convertToFloatArray(byte[] bytes) {
        FloatBuffer fb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
        float[] array = new float[fb.remaining()];
        fb.get(array);
        return array;
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