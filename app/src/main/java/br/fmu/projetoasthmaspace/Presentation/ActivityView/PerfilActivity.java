package br.fmu.projetoasthmaspace.Presentation.ActivityView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import br.fmu.projetoasthmaspace.Core.Session.UserServiceHelper;
import br.fmu.projetoasthmaspace.Core.Session.UserSessionManager;
import br.fmu.projetoasthmaspace.Presentation.Fragment.DiarioSintomasFragment;
import br.fmu.projetoasthmaspace.R;
import br.fmu.projetoasthmaspace.databinding.ActivityPerfilBinding;

public class PerfilActivity extends Fragment {

    private ActivityPerfilBinding binding;

    private final ActivityResultLauncher<String> getContentLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            salvarFotoLocalmente(uri);
                        }
                    }
            );

    private void salvarFotoLocalmente(Uri uri) {
        try {
            // Cria o arquivo de destino no armazenamento interno do app
            File diretorio = requireContext().getFilesDir();
            File arquivoFoto = new File(diretorio, "foto_perfil.jpg");

            // Copia os bytes da Uri para o arquivo interno
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(arquivoFoto);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();

            // Salva o caminho na sessão
            new UserSessionManager(requireContext()).saveFotoPath(arquivoFoto.getAbsolutePath());

            // Exibe a foto
            binding.imgPerfil.setImageURI(Uri.fromFile(arquivoFoto));

        } catch (Exception e) {
            Log.e("PERFIL", "Erro ao salvar foto: " + e.getMessage());
            Toast.makeText(requireContext(), "Erro ao salvar foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarFotoPerfil() {
        String path = new UserSessionManager(requireContext()).getFotoPath();
        if (path != null) {
            File arquivo = new File(path);
            if (arquivo.exists()) {
                binding.imgPerfil.setImageURI(Uri.fromFile(arquivo));
            }
        }
    }

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

        carregarFotoPerfil();
        carregarDadosDoPerfil();

        binding.imgPerfil.setOnClickListener(v -> {
            getContentLauncher.launch("image/*");
        });

        // botao tela "informações pessoais"
        binding.btnInfPessoais.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), InformacoesPessoaisActivity.class);
            startActivity(intent);
        });

        binding.btnConfig.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ConfiguracoesActivity.class);
            startActivity(intent);
        });

        binding.btnAjuda.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AjudaActivity.class);
            startActivity(intent);
        });

        binding.btnDiarioSintomas.setOnClickListener(v -> {

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, new DiarioSintomasFragment())
                    .addToBackStack(null)
                    .commit();


            ((MainActivity) requireActivity()).binding.bottomNavigationView
                    .setSelectedItemId(R.id.navigation_diario);
        });

        binding.btnNotificacoes.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), NotificacoesActivity.class);
            startActivity(intent);
        });



        binding.btnSair.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Sair do Aplicativo")
                    .setMessage("Deseja realmente sair?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        new UserSessionManager(getContext()).logout(getContext());
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });

    }

    private void carregarDadosDoPerfil() {

        UserSessionManager session = new UserSessionManager(requireContext());
        String nomeCompleto = session.getNome();
        String token = session.getToken();

        if (nomeCompleto != null && !nomeCompleto.trim().isEmpty()) {

            binding.textName.setText(nomeCompleto);

        } else if (token != null) {

            UserServiceHelper.buscarNomeUsuario(
                    requireContext(),
                    new UserServiceHelper.NomeCallback() {

                        @Override
                        public void onSuccess(String nome) {
                            requireActivity().runOnUiThread(() ->
                                    binding.textName.setText(nome)
                            );
                        }

                        @Override
                        public void onError(String erro) {
                            Log.e("PERFIL", erro);
                        }
                    });

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
