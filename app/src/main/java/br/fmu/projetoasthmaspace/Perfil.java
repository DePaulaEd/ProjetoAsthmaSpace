package br.fmu.projetoasthmaspace;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import br.fmu.projetoasthmaspace.Domain.TokenManager;
import br.fmu.projetoasthmaspace.databinding.ActivityPerfilBinding;

public class Perfil extends Fragment {

    private ActivityPerfilBinding binding;

    private final ActivityResultLauncher<String> getContentLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            binding.imgPerfil.setImageURI(uri);
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ActivityPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        carregarDadosDoPerfil();

        // foto de perfil
        binding.imgPerfil.setOnClickListener(v -> {
            getContentLauncher.launch("image/*");
        });

        // botao tela "informações pessoais"
        binding.btnInfPessoais.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), InformacoesPessoais.class);
            startActivity(intent);
        });

        // botao de sair do app
        binding.btnSair.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Sair do Aplicativo")
                    .setMessage("Deseja realmente sair?")
                    .setPositiveButton("Sim", (dialog, which) -> {

                        // LIMPAR TOKEN AO SAIR
                        TokenManager.clearToken(getContext());

                        // Ir para a tela de Login limpando o histórico
                        Intent intent = new Intent(getActivity(), Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        getActivity().finish();
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });

    }

    private void carregarDadosDoPerfil() {
        // Carrega o nome do usuário salvo
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String nomeCompleto = prefs.getString("user_name", null);

        if (nomeCompleto != null && !nomeCompleto.trim().isEmpty()) {
            binding.textName.setText(nomeCompleto);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
