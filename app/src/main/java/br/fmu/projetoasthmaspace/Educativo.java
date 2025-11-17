package br.fmu.projetoasthmaspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import br.fmu.projetoasthmaspace.databinding.FragmentEducativoBinding;

public class Educativo extends Fragment {

    private FragmentEducativoBinding binding;
    private List<Artigo> artigos = new ArrayList<>();

    // Modelo de dados para um Artigo, agora com conteúdo completo
    private static class Artigo {
        String titulo;
        String resumo;
        String conteudo;
        int imagemResId;

        Artigo(String titulo, String resumo, String conteudo, @DrawableRes int imagemResId) {
            this.titulo = titulo;
            this.resumo = resumo;
            this.conteudo = conteudo;
            this.imagemResId = imagemResId;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEducativoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preencherListaDeArtigos();

        for (Artigo artigo : artigos) {
            adicionarArtigo(artigo);
        }
    }

    private void preencherListaDeArtigos() {
        String conteudoLongo = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

        artigos.add(new Artigo("Aonde Guardar seu Inalador", "Um guia sobre os melhores locais para armazenar seu inalador para garantir sua eficácia.", conteudoLongo, R.drawable.img_inalador));
        artigos.add(new Artigo("Como Limpar seu Espaçador", "Um guia passo a passo para manter seu espaçador limpo e seguro para uso.", conteudoLongo, R.drawable.img_inalador));
        artigos.add(new Artigo("Exercícios de Respiração", "Técnicas de respiração que podem ajudar a controlar os sintomas da asma.", conteudoLongo, R.drawable.img_inalador));
        artigos.add(new Artigo("Gatilhos Comuns da Asma", "Identifique e evite os gatilhos mais comuns que podem levar a uma crise de asma.", conteudoLongo, R.drawable.img_inalador));
        artigos.add(new Artigo("Plano de Ação para Asma", "Aprenda a criar um plano de ação personalizado com seu médico.", conteudoLongo, R.drawable.img_inalador));
    }

    private void adicionarArtigo(final Artigo artigo) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_artigo_educativo, binding.containerArtigos, false);

        ImageView artigoImagem = itemView.findViewById(R.id.artigo_imagem);
        TextView artigoTitulo = itemView.findViewById(R.id.artigo_titulo);
        TextView artigoResumo = itemView.findViewById(R.id.artigo_resumo);
        Button artigoBtnLer = itemView.findViewById(R.id.artigo_btn_ler);

        artigoImagem.setImageResource(artigo.imagemResId);
        artigoTitulo.setText(artigo.titulo);
        artigoResumo.setText(artigo.resumo); // Mostra o resumo na lista

        // Ao clicar, passa o CONTEÚDO completo para o diálogo
        artigoBtnLer.setOnClickListener(v -> showArtigoDialog(artigo.titulo, artigo.conteudo));

        binding.containerArtigos.addView(itemView);
    }

    private void showArtigoDialog(String titulo, String conteudo) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_artigo_educativo, null);

        TextView dialogTitulo = dialogView.findViewById(R.id.dialog_titulo);
        TextView dialogConteudo = dialogView.findViewById(R.id.dialog_conteudo);
        Button dialogBtnFechar = dialogView.findViewById(R.id.dialog_btn_fechar);

        dialogTitulo.setText(titulo);
        dialogConteudo.setText(conteudo);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        dialogBtnFechar.setOnClickListener(v_fechar -> alertDialog.dismiss());

        alertDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
