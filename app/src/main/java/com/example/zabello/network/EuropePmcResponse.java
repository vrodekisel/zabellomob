package com.example.zabello.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Модели ответа Europe PMC (уровней достаточно для нашего поиска). */
public class EuropePmcResponse {
    @SerializedName("resultList")
    public ResultList resultList;

    public static class ResultList {
        @SerializedName("result")
        public List<Item> result;
    }

    public static class Item {
        @SerializedName("id") public String id;
        @SerializedName("source") public String source;          // MED, PMC, PAT, AGR, etc.
        @SerializedName("title") public String title;
        @SerializedName("abstractText") public String abstractText;

        @SerializedName("keywordList") public KeywordList keywordList;
        @SerializedName("meshHeadingList") public MeshHeadingList meshHeadingList;
    }

    public static class KeywordList {
        @SerializedName("keyword") public List<String> keyword;
    }

    public static class MeshHeadingList {
        @SerializedName("meshHeading") public List<MeshHeading> meshHeading;
    }

    public static class MeshHeading {
        @SerializedName("descriptorName") public String descriptorName;
    }
}
