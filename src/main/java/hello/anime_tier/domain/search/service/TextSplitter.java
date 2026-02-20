package hello.anime_tier.domain.search.service;

import java.util.List;

public interface TextSplitter {
    List<String> split(String text);
}
