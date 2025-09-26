package com.example.zabello.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zabello.R;
import com.example.zabello.data.entity.Article;
import com.example.zabello.ui.list.ArticleAdapter;
import com.example.zabello.utils.FilterPrefs;
import com.example.zabello.viewmodel.ReferenceViewModel;

import java.util.List;

public class ReferenceFragment extends Fragment {

    private ReferenceViewModel vm;
    private ArticleAdapter adapter;
    private FilterPrefs prefs;

    private EditText etSearch;
    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reference, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = new FilterPrefs(requireContext());

        etSearch = view.findViewById(R.id.etSearch);
        rv = view.findViewById(R.id.rvArticles);
        progress = view.findViewById(R.id.progress);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ArticleAdapter(this::openDetails);
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(requireActivity()).get(ReferenceViewModel.class);
        vm.getArticles().observe(getViewLifecycleOwner(), this::render);

        // Restore query
        String last = prefs.getRefQuery();
        if (last != null && !last.isEmpty()) {
            etSearch.setText(last);
            etSearch.setSelection(last.length());
            vm.setQuery(last);
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s != null ? s.toString() : "";
                prefs.setRefQuery(q);
                // показать индикатор при потенциальном удалённом поиске
                progress.setVisibility(q.trim().length() >= 2 ? View.VISIBLE : View.GONE);
                vm.setQuery(q);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void render(List<Article> list) {
        progress.setVisibility(View.GONE);
        adapter.submitList(list);
        tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openDetails(Article a) {
        Bundle b = new Bundle();
        b.putString("slug", a.slug);
        Navigation.findNavController(requireView()).navigate(R.id.action_reference_to_articleDetails, b);
    }
}
