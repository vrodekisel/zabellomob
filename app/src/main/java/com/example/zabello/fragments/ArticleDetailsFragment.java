package com.example.zabello.fragments;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.zabello.R;
import com.example.zabello.data.entity.Article;
import com.example.zabello.repository.HealthRepository;

public class ArticleDetailsFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_article_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        TextView tvTitle = v.findViewById(R.id.tvTitle);
        TextView tvBody  = v.findViewById(R.id.tvBody);
        TextView tvTags  = v.findViewById(R.id.tvTags);
        tvBody.setMovementMethod(new ScrollingMovementMethod());

        String slug = getArguments() != null ? getArguments().getString("slug") : null;
        if (slug == null) return;

        new ViewModelProvider(requireActivity()); // keep lifecycle owner
        new HealthRepository(requireContext()).getArticleBySlug(slug)
                .observe(getViewLifecycleOwner(), article -> {
                    if (article == null) return;
                    tvTitle.setText(article.title);
                    tvBody.setText(article.body);
                    tvTags.setText(article.tags != null ? article.tags : "");
                });
    }
}
