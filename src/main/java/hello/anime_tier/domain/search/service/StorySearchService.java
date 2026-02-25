package hello.anime_tier.domain.search.service;


import hello.anime_tier.entity.SynopsisChunkEntity;
import hello.anime_tier.repository.SynopsisChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorySearchService {
    private final SynopsisChunkRepository synopsisChunkRepository;
    private final TransformersEmbeddingModel embeddingModel;

    public List<String> searchAnime(String query){
        //1. 사용자 질문 벡터변환
        float[] queryVector = embeddingModel.embed(query);
        String vec = toPgVectorLiteral(queryVector);

        //2. DB에서 벡터 유사도 검색 수행 (Top 5)
        return synopsisChunkRepository.findTopKAnimeTitlesByVector(vec, 20);
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


    public java.util.Map<Integer, Integer> getStoryTop20RankMap(String query) {
        float[] queryVector = embeddingModel.embed(query);
        String vec = toPgVectorLiteral(queryVector);

        // 중복 때문에 넉넉히 뽑기
        List<Integer> raw = synopsisChunkRepository.findTopKAnimeIdsByVector(vec, 100);

        java.util.Map<Integer, Integer> rankMap = new java.util.HashMap<>();
        int rank = 1;
        for (Integer animeId : raw) {
            if (!rankMap.containsKey(animeId)) {
                rankMap.put(animeId, rank);
                rank++;
                if (rank > 20) break;
            }
        }
        return rankMap;
    }







}
