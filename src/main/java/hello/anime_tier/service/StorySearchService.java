package hello.anime_tier.service;


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

        //2. 데이터베이스에서 모든 청크 데이터 로드
        List<SynopsisChunkEntity> allChunks = synopsisChunkRepository.findAll();

        //3. 유사도 계산 및 정렬
        return allChunks.stream()
                .filter(chunk -> chunk.getEmbedding() != null)
                .map( chunk -> {
                    float[] chunkVector = convertToFloatArray(chunk.getEmbedding());
                    double score = calculateCosineSimilarity(queryVector, chunkVector);
                    return new SearchResult(chunk.getAnime().getTitleEn(), score);
                })
                .sorted((a,b) -> Double.compare(b.score, a.score))
                .limit(5)
                .map(res -> res.title)
                .toList();

    }

    private float[] convertToFloatArray(byte[] bytes) {
        FloatBuffer fb = ByteBuffer.wrap(bytes).asFloatBuffer();

        float[] array = new float[fb.remaining()];

        fb.get(array);
        return array;
    }

    //코사인 유사도 공식
    private double calculateCosineSimilarity(float[] v1, float[] v2) {
        double dotProduct = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += Math.pow(v1[i], 2);
            normB += Math.pow(v2[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }



    private record SearchResult(String title, double score) {}

}
