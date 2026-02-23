package hello.anime_tier.domain.save.service;

import hello.anime_tier.entity.SynopsisChunkEntity;
import hello.anime_tier.entity.TagEntity;
import hello.anime_tier.repository.SynopsisChunkRepository;
import hello.anime_tier.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import com.pgvector.PGvector;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {
    private final SynopsisChunkRepository synopsisChunkRepository;
    private final TagRepository tagRepository;

    // Spring AI가 'application.properties' 설정을 바탕으로
    // 로컬 ONNX 모델을 로드하여 자동으로 생성해주는 빈(Bean)입니다.
    private final EmbeddingModel embeddingModel;



    /**
     * DB에 저장된 모든 줄거리 청크를 가져와서
     * AI 모델을 통해 벡터(임베딩)로 변환한 뒤 저장합니다.
     */
    @Transactional
    public void createEmbeddingsForSynopsis() {
        // 1. 아직 임베딩이 되지 않은(null인) 데이터만 가져오거나 전체를 가져옵니다.
        List<SynopsisChunkEntity> chunks = synopsisChunkRepository.findAll();

        log.info("총 {}개의 문단에 대해 임베딩 작업을 시작합니다.", chunks.size());

        for (SynopsisChunkEntity chunk : chunks) {
            // 2. 이미 정제된(HTML 태그가 제거된) 텍스트를 가져옵니다.
            String text = chunk.getChunkText();

            if (text == null || text.isEmpty()) continue;

            try {
                // 3. 임베딩 모델 호출 (가장 중요한 핵심!)
                // 입력: "The fourth season of Kingdom..." (영어 문장)
                // 결과: [0.123, -0.456, 0.789, ...] (다국어를 지원하는 숫자 좌표 배열)
                // 이 과정에서 사용자의 CPU 점유율이 일시적으로 올라갑니다.
                float[] vector = embeddingModel.embed(text);

//                // 4. float[] 배열을 MySQL의 BLOB 타입에 저장하기 위해 byte[]로 변환합니다.
//                byte[] byteVector = convertToBytes(vector);

                // 5. 엔티티에 세팅 (Dirty Checking에 의해 메서드 종료 시 자동 DB 저장)
                // chunk.setEmbedding(vector);
                chunk.setEmbedding(vector);

                log.info("애니메이션 ID {}의 문단 임베딩 완료", chunk.getAnime().getAnimeId());

            } catch (Exception e) {
                log.error("임베딩 생성 중 오류 발생: {}", e.getMessage());
            }
        }

        log.info("모든 임베딩 작업이 완료되었습니다.");
    }



    @Transactional
    public void createEmbeddingsForTags() {
        List<TagEntity> tags = tagRepository.findAll();

        log.info("총 {}개의 태그에 대해 임베딩 작업을 시작합니다.", tags.size());

        for(TagEntity tag : tags){
            String tagName = tag.getTagName();

            if(tagName == null || tagName.isEmpty()) continue;


            try{
                float[] vector = embeddingModel.embed(tagName);

//                byte[] byteVector = convertToBytes(vector);

                tag.setTagEmbedding(vector);
                log.info("애니메이션 태그 {} 임베딩 완료", tag.getTagName());
            } catch (Exception e) {
                log.error("임베딩 중 생성 오류 발생: {}", e.getMessage());
            }

            log.info("모든 임베딩 작업이 완료되었습니다.");

        }




    }

    /**
     * float[] 배열을 이진 데이터(byte[])로 변환하는 유틸리티 메서드
     * 자바의 float(4바이트)을 0과 1의 데이터 덩어리로 직렬화합니다.
     */
    private byte[] convertToBytes(float[] floatArray) {
        // float 하나당 4바이트이므로 배열 길이 * 4만큼 공간 확보
        ByteBuffer byteBuffer = ByteBuffer.allocate(floatArray.length * 4);

        // Byte 데이터를 Float처럼 다룰 수 있게 변환
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();

        // float 배열의 모든 값을 버퍼에 집어넣음
        floatBuffer.put(floatArray);

        // 최종적으로 바이트 배열로 변환해서 반환
        return byteBuffer.array();
    }


}
