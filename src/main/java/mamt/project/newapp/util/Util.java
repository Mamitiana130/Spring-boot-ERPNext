package mamt.project.newapp.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
public class Util {

    public static String formatDate(String inputDate) throws Exception {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
            inputFormat.setLenient(false);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            throw new Exception("Format de date invalide : " + inputDate + ". Le format attendu est dd/MM/yyyy.");
        }
    }
}
