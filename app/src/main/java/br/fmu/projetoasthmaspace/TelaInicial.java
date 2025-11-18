package br.fmu.projetoasthmaspace;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import br.fmu.projetoasthmaspace.databinding.ActivityTelaInicialBinding;
import br.fmu.projetoasthmaspace.databinding.ItemPoluenteBinding;

public class TelaInicial extends Fragment {

    private ActivityTelaInicialBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityTelaInicialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preencherDadosExemplo();
        criarLinkEducativo();
    }

    private void preencherDadosExemplo() {
        int aqiExemplo = 121;
        String statusExemplo = "Ruim";
        String recomendacaoExemplo = gerarRecomendacao(aqiExemplo);

        binding.textLocalizacao.setText("São Paulo, SP");
        binding.textAqiValor.setText(String.valueOf(aqiExemplo));
        binding.textAqiStatus.setText(statusExemplo);
        binding.textRecomendacao.setText(recomendacaoExemplo);

        setupPoluenteCard(binding.poluentePm25, "PM2.5", "Partículas Finas", "45 µg/m³", "Ruim");
        setupPoluenteCard(binding.poluentePm10, "PM10", "Partículas Inaláveis", "80 µg/m³", "Moderado");
        setupPoluenteCard(binding.poluenteO3, "O₃", "Ozônio", "130 µg/m³", "Ruim");
        setupPoluenteCard(binding.poluenteNo2, "NO₂", "Dióxido de Nitrogênio", "50 µg/m³", "Bom");
        setupPoluenteCard(binding.poluenteSo2, "SO₂", "Dióxido de Enxofre", "10 µg/m³", "Bom");
        setupPoluenteCard(binding.poluenteCo, "CO", "Monóxido de Carbono", "2 mg/m³", "Bom");
    }

    private void criarLinkEducativo() {
        String textoCompleto = "Não entende o que esses dados significam? Saiba mais.";
        SpannableString spannableString = new SpannableString(textoCompleto);
        
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Navega para o fragmento Educativo
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout, new Educativo());
                fragmentTransaction.addToBackStack(null); // Permite voltar para a tela inicial
                fragmentTransaction.commit();
            }
        };

        String textoLink = "Saiba mais.";
        int inicioDoLink = textoCompleto.indexOf(textoLink);
        int fimDoLink = inicioDoLink + textoLink.length();

        if (inicioDoLink != -1) {
            spannableString.setSpan(clickableSpan, inicioDoLink, fimDoLink, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        binding.textLinkEducativo.setText(spannableString);
        binding.textLinkEducativo.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private String gerarRecomendacao(int aqi) {
        if (aqi <= 50) {
            return "Qualidade do ar ideal. Aproveite para se exercitar ao ar livre!";
        } else if (aqi <= 100) {
            return "Qualidade do ar aceitável. Indivíduos sensíveis podem sentir algum desconforto.";
        } else if (aqi <= 150) {
            return "Evite atividades ao ar livre prolongadas. Grupos sensíveis devem permanecer em ambientes fechados.";
        } else {
            return "Risco elevado à saúde. Evite qualquer atividade ao ar livre e use máscara se precisar sair.";
        }
    }

    private void setupPoluenteCard(ItemPoluenteBinding cardBinding, String sigla, String nomeCompleto, String valor, String status) {
        cardBinding.poluenteSigla.setText(sigla);
        cardBinding.poluenteNomeCompleto.setText(nomeCompleto);
        cardBinding.poluenteValor.setText(valor);
        cardBinding.poluenteStatus.setText(status);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
