package br.fmu.projetoasthmaspace.Domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiarioParser {

    private static final SimpleDateFormat DATA_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final SimpleDateFormat HORARIO_FORMAT =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private static final SimpleDateFormat DATA_HORA_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


    public static Date parseData(String data) {
        try {
            return DATA_FORMAT.parse(data);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parseHorario(String horario) {
        try {
            return HORARIO_FORMAT.parse(horario);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parseDataHora(String data, String horario) {
        try {
            return DATA_HORA_FORMAT.parse(data + " " + horario);
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean isToday(String data) {
        try {
            Date d = DATA_FORMAT.parse(data);

            // pega somente a parte da data (sem horas)
            String hojeStr = DATA_FORMAT.format(new Date());
            String dataStr = DATA_FORMAT.format(d);

            return hojeStr.equals(dataStr);

        } catch (Exception e) {
            return false;
        }
    }
}
