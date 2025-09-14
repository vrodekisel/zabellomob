package com.example.zabello.network;

import com.example.zabello.data.entity.Article;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Преобразование результатов Europe PMC -> локальная сущность Article. */
public final class RemoteArticleMapper {
    private RemoteArticleMapper() {}

    public static List<Article> toEntities(EuropePmcResponse resp) {
        List<Article> out = new ArrayList<>();
        if (resp == null || resp.resultList == null || resp.resultList.result == null) return out;
        for (EuropePmcResponse.Item it : resp.resultList.result) {
            if (it == null) continue;
            Article a = new Article();
            String src = (it.source != null ? it.source : "SRC");
            String id  = (it.id != null ? it.id : "");
            a.slug  = "epmc:" + src + ":" + id; // стабильный ключ для upsert
            a.title = it.title != null ? it.title.trim() : "(no title)";
            a.body  = it.abstractText != null ? it.abstractText.trim() : "";
            a.tags  = collectTags(it);
            out.add(a);
        }
        return out;
    }

    private static String collectTags(EuropePmcResponse.Item it) {
        Set<String> tags = new HashSet<>();
        if (it.keywordList != null && it.keywordList.keyword != null) {
            for (String k : it.keywordList.keyword) if (k != null && !k.trim().isEmpty()) tags.add(k.trim());
        }
        if (it.meshHeadingList != null && it.meshHeadingList.meshHeading != null) {
            for (EuropePmcResponse.MeshHeading m : it.meshHeadingList.meshHeading) {
                if (m != null && m.descriptorName != null && !m.descriptorName.trim().isEmpty()) tags.add(m.descriptorName.trim());
            }
        }
        if (tags.isEmpty()) return null;
        return tags.stream().collect(Collectors.joining(", "));
    }
}
