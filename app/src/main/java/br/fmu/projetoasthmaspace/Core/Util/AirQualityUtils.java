package br.fmu.projetoasthmaspace.Core.Util;

import java.util.Random;

public class AirQualityUtils {

    private static final Random random = new Random();

    public static String gerarRecomendacaoAqi(int aqi) {
        if (aqi == 1) {
            String[] mensagens = {
                    "🌿 Qualidade do ar excelente! Ótimo momento para atividades ao ar livre, caminhadas e exercícios. Pessoas com asma podem respirar com tranquilidade.",
                    "🌤️ O ar está limpo e seguro para todos. Aproveite para se exercitar ou passear sem preocupações.",
                    "✅ Condições ideais! Sem restrições para nenhum grupo. Asmáticos podem realizar suas atividades normalmente sem risco de crise."
            };
            return mensagens[random.nextInt(mensagens.length)];

        } else if (aqi == 2) {
            String[] mensagens = {
                    "🟡 Ar razoável. Atividades leves ao ar livre são seguras para a maioria. Pessoas sensíveis devem evitar esforços prolongados.",
                    "🟡 Qualidade aceitável. Asmáticos e idosos podem notar leve desconforto em atividades intensas. Mantenha seu inalador por perto.",
                    "🟡 Condições moderadas. Observe como seu corpo reage e evite horários de pico de tráfego."
            };
            return mensagens[random.nextInt(mensagens.length)];

        } else if (aqi == 3) {
            String[] mensagens = {
                    "🟠 Atenção! O ar pode causar desconforto respiratório. Asmáticos e crianças devem evitar exercícios ao ar livre.",
                    "🟠 Qualidade do ar moderada. Reduza o tempo de exposição externa e mantenha os medicamentos em mãos.",
                    "🟠 Cuidado redobrado para asmáticos. Prefira atividades internas e não esqueça o inalador."
            };
            return mensagens[random.nextInt(mensagens.length)];

        } else if (aqi == 4) {
            String[] mensagens = {
                    "🔴 Qualidade do ar ruim. Evite atividades ao ar livre. Asmáticos devem permanecer em locais fechados e ter os medicamentos sempre acessíveis.",
                    "🔴 Ar prejudicial à saúde. Mantenha janelas fechadas e use purificador se possível. Ao menor sinal de crise, procure atendimento médico.",
                    "🔴 Risco elevado para grupos sensíveis. Não realize atividades externas e reforce o uso dos medicamentos preventivos."
            };
            return mensagens[random.nextInt(mensagens.length)];

        } else {
            String[] mensagens = {
                    "⛔ Emergência respiratória! Permaneça em ambientes fechados. Asmáticos devem acionar suporte médico ao menor sinal de crise.",
                    "⛔ Qualidade do ar muito ruim. Use máscara N95 se precisar sair e mantenha janelas e portas bem vedadas.",
                    "⛔ Risco grave à saúde. Evite qualquer exposição externa e tenha os medicamentos de emergência sempre à mão."
            };
            return mensagens[random.nextInt(mensagens.length)];
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