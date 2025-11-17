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
        artigos.add(new Artigo(
                "Aonde Guardar seu Inalador",
                "Um guia sobre os melhores locais para armazenar seu inalador.",
                "Para garantir a eficácia do seu inalador, guarde-o em um local seco e em temperatura ambiente. Evite locais úmidos como o banheiro, ou locais muito quentes, como o porta-luvas do carro. Mantenha-o longe da luz solar direta e certifique-se de que a tampa do bocal esteja sempre no lugar para protegê-lo de poeira e sujeira.",
                R.drawable.img_inalador
        ));
        artigos.add(new Artigo(
                "Como Limpar seu Espaçador",
                "Um guia passo a passo para manter seu espaçador limpo e seguro.",
                "Limpar seu espaçador é crucial para evitar o acúmulo de mofo e bactérias. Desmonte-o e lave as peças em água morna com um pouco de detergente neutro. Enxágue bem e deixe secar completamente ao ar livre, sem usar panos. A limpeza deve ser feita pelo menos uma vez por semana.",
                R.drawable.img_inalador
        ));
        artigos.add(new Artigo(
                "Exercícios de Respiração",
                "Técnicas de respiração que podem ajudar a controlar os sintomas.",
                "Exercícios como a respiração diafragmática podem fortalecer seus pulmões. Sente-se confortavelmente, coloque uma mão no peito e outra na barriga. Inspire lentamente pelo nariz, sentindo sua barriga se expandir. Expire lentamente pela boca. Praticar de 5 a 10 minutos por dia pode ajudar a melhorar sua capacidade respiratória e a gerenciar a falta de ar.",
                R.drawable.img_inalador
        ));
        artigos.add(new Artigo(
                "Gatilhos Comuns da Asma",
                "Identifique e evite os gatilhos mais comuns que podem levar a uma crise.",
                "Os gatilhos da asma variam de pessoa para pessoa, mas os mais comuns incluem poeira, pólen, mofo, pelos de animais, fumaça de cigarro, poluição do ar, ar frio e exercícios intensos. Manter um diário de sintomas pode ajudá-lo a identificar seus gatilhos específicos para que você possa evitá-los de forma mais eficaz.",
                R.drawable.img_inalador
        ));
        artigos.add(new Artigo(
                "Plano de Ação para Asma",
                "Aprenda a criar um plano de ação personalizado com seu médico.",
                "Um plano de ação para asma é um guia escrito que você desenvolve com seu médico. Ele detalha seus medicamentos diários, como lidar com o agravamento dos sintomas e o que fazer em caso de uma crise de asma. Ter um plano claro e acessível é uma das ferramentas mais importantes para gerenciar sua condição com segurança.",
                R.drawable.img_inalador
        ));
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
        artigoResumo.setText(artigo.resumo);

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
