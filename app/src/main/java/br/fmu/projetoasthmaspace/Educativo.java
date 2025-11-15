package br.fmu.projetoasthmaspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import br.fmu.projetoasthmaspace.databinding.FragmentEducativoBinding;

public class Educativo extends Fragment {

    private FragmentEducativoBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEducativoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Lógica do fragmento Educativo aqui
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
