package br.fmu.projetoasthmaspace.Domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DiarioParser {

    private static final TimeZone TZ = TimeZone.getTimeZone("America/Sao_Paulo");

    private static final SimpleDateFormat DATA_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final SimpleDateFormat HORARIO_FORMAT =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private static final SimpleDateFormat DATA_HORA_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // 👉 APLICAR TIMEZONE EM TODOS OS FORMATTERS
    static {
        DATA_FORMAT.setTimeZone(TZ);
        HORARIO_FORMAT.setTimeZone(TZ);
        DATA_HORA_FORMAT.setTimeZone(TZ);
    }

    public static Date parseData(String data) {
        try {
            return DATA_FORMAT.parse(data);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parseHorario(String horario) {
        try {
            return HORARIO_FORMAT.parse(horario + ":00"); // garante HH:mm:ss
        } catch (Exception e) {
            return null;
        }
    }

    public static Date parseDataHora(String data, String horario) {
        try {
            return DATA_HORA_FORMAT.parse(data + " " + horario + ":00");
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isToday(String data) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

            // Data recebida do backend
            Date dataLembrete = sdf.parse(data);

            // Data atual no fuso correto
            String hojeStr = sdf.format(new Date());
            String dataStr = sdf.format(dataLembrete);

            return hojeStr.equals(dataStr);

        } catch (Exception e) {
            return false;
        }
    }

}
