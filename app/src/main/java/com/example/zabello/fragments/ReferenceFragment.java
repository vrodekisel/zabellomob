package com.example.zabello.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
import com.example.zabello.viewmodel.ReferenceViewModel;

import java.util.List;

public class ReferenceFragment extends Fragment {

    private ReferenceViewModel vm;
    private ArticleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // ВАЖНО: инфлейтим R.layout.fragment_reference
        return inflater.inflate(R.layout.fragment_reference, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        RecyclerView rv = v.findViewById(R.id.rvArticles);
        EditText etSearch = v.findViewById(R.id.etSearch);

        // Если id не найдены — это сразу заметно и не упадёт втихую
        if (rv == null) throw new IllegalStateException("fragment_reference.xml должен содержать RecyclerView с id=rvArticles");
        if (etSearch == null) throw new IllegalStateException("fragment_reference.xml должен содержать EditText с id=etSearch");

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ArticleAdapter(this::openDetails);
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(requireActivity()).get(ReferenceViewModel.class);
        vm.getArticles().observe(getViewLifecycleOwner(), this::render);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                vm.setQuery(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void render(List<Article> list) {
        adapter.submitList(list);
    }

    private void openDetails(Article a) {
        Bundle b = new Bundle();
        b.putString("slug", a.slug);
        Navigation.findNavController(requireView()).navigate(R.id.action_reference_to_articleDetails, b);
    }
}
