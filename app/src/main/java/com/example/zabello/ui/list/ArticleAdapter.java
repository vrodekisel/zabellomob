package com.example.zabello.ui.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zabello.R;
import com.example.zabello.data.entity.Article;

public class ArticleAdapter extends ListAdapter<Article, ArticleAdapter.VH> {

    public interface OnClick { void onClick(Article a); }

    private final OnClick onClick;

    public ArticleAdapter(OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    private static final DiffUtil.ItemCallback<Article> DIFF =
            new DiffUtil.ItemCallback<Article>() {
                @Override public boolean areItemsTheSame(@NonNull Article o, @NonNull Article n) { return o.id == n.id; }
                @Override public boolean areContentsTheSame(@NonNull Article o, @NonNull Article n) {
                    return o.slug.equals(n.slug) && o.title.equals(n.title) && o.body.equals(n.body)
                            && ((o.tags == null && n.tags == null) || (o.tags != null && o.tags.equals(n.tags)));
                }
            };

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        Article a = getItem(position);
        h.tvTitle.setText(a.title);
        h.tvTags.setText(a.tags != null ? a.tags : "");
        h.itemView.setOnClickListener(v -> onClick.onClick(a));
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvTags;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTags  = itemView.findViewById(R.id.tvTags);
        }
    }
}
