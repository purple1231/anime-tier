package hello.anime_tier.domain.search.service;

import org.springframework.stereotype.Service;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SentenceSplitService implements TextSplitter {

    @Override
    public List<String> split(String text) {
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
}