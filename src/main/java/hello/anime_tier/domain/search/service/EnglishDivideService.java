package hello.anime_tier.domain.search.service;

import org.springframework.stereotype.Service;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class EnglishDivideService implements TextSplitter {

    private static final Pattern CLAUSE_SPLIT = Pattern.compile(
            "\\s*(?:,|/|\\n|\\r\\n|\\band\\b|\\bor\\b|\\bbut\\b|\\bwith\\b|\\bwithout\\b|\\bexcept\\b|\\binstead of\\b|\\blike\\b|\\bsimilar to\\b)\\s*",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public List<String> split(String text) {
        if (text == null) return List.of();
        text = text.trim();
        if (text.isEmpty()) return List.of();

        // 1️⃣ Sentence split (strong boundary)
        List<String> chunks = new ArrayList<>();
        BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.ENGLISH);
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
        return mergeTooShort(clauses, 6);
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
