package br.fmu.projetoasthmaspace.Domain;

public class AirQualityUtils {

    public static String gerarRecomendacaoAqi(int aqi) {
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

    public static String statusPm10(double v) {
        if (v <= 50) return "Bom";
        if (v <= 100) return "Moderado";
        return "Ruim";
    }

    public static String statusO3(double v) {
        if (v <= 100) return "Bom";
        if (v <= 160) return "Moderado";
        return "Ruim";
    }

    public static String statusNo2(double v) {
        if (v <= 40) return "Bom";
        if (v <= 100) return "Moderado";
        return "Ruim";
    }

    public static String statusSo2(double v) {
        if (v <= 20) return "Bom";
        if (v <= 80) return "Moderado";
        return "Ruim";
    }

    public static String statusCo(double v) {
        if (v <= 4) return "Bom";
        if (v <= 9) return "Moderado";
        return "Ruim";
    }

    public static String statusPm25(double v) {
        if (v <= 12) return "Bom";
        if (v <= 25) return "Moderado";
        return "Ruim";
    }

    public static String statusAqi(int aqi) {
        switch (aqi) {
            case 1: return "Bom";
            case 2: return "Razoável";
            case 3: return "Moderado";
            case 4: return "Ruim";
            default: return "Muito Ruim";
        }
    }
}