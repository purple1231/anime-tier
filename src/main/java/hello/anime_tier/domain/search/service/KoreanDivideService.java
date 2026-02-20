package hello.anime_tier.domain.search.service;

import org.springframework.stereotype.Service;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class KoreanDivideService implements TextSplitter {

    // 한국어 접속사 및 연결 어미 패턴 (예: 그리고, 또는, ~고, ~며 등)
    private static final Pattern CLAUSE_SPLIT = Pattern.compile(
            "\\s*(?:,|/|\\n|\\r\\n|그리고|또는|하지만|그런데|따라서|왜냐하면|비슷하게|처럼|같이|~고|~며|~으나|~는데)\\s*",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public List<String> split(String text) {
        if (text == null) return List.of();
        text = text.trim();
        if (text.isEmpty()) return List.of();

        // 1️⃣ Sentence split (strong boundary) - 한국어 Locale 사용
        List<String> chunks = new ArrayList<>();
        BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.KOREAN);
        boundary.setText(text);

        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            String sentence = text.substring(start, end).trim();
            if (!sentence.isEmpty()) {
                chunks.add(sentence);
            }
        }

        if (chunks.isEmpty()) {
            chunks.add(text);
        }

        // 2️⃣ Clause split (condition level)
        List<String> clauses = new ArrayList<>();
        for (String chunk : chunks) {
            String[] parts = CLAUSE_SPLIT.split(chunk);
            for (String part : parts) {
                String cleaned = part.trim();
                if (!cleaned.isEmpty()) {
                    clauses.add(cleaned);
                }
            }
        }

        // 3️⃣ Merge too short fragments (optional but recommended)
        // 한국어는 영어보다 글자 수가 적어도 의미 전달이 잘 되므로 minLength를 조금 줄여도 됨 (예: 4)
        return mergeTooShort(clauses, 4);
    }

    private List<String> mergeTooShort(List<String> parts, int minLength) {
        List<String> result = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        for (String part : parts) {
            if (buffer.length() == 0) {
                buffer.append(part);
            } else if (buffer.length() < minLength) {
                buffer.append(" ").append(part);
            } else {
                result.add(buffer.toString().trim());
                buffer.setLength(0);
                buffer.append(part);
            }
        }

        if (buffer.length() > 0) {
            result.add(buffer.toString().trim());
        }

        return result;
    }
}
