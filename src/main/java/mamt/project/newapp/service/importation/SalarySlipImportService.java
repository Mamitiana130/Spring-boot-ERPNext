package mamt.project.newapp.service.importation;


import mamt.project.newapp.util.Util;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalarySlipImportService {
    public List<Map<String, Object>> prepareSalarySlip(MultipartFile file)throws Exception{
        List<Map<String, Object>> salaryStructures = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(',')
                     .withFirstRecordAsHeader()
                     .withIgnoreEmptyLines(true)
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                try {
                    String  mois= record.get("Mois").trim();
                    if(mois.isEmpty()){
                        throw new Exception(file.getOriginalFilename() +" Ligne ignorée : Mois vide. Ligne " + record.getRecordNumber());
                    }
                    String ref = record.get("Ref Employe").trim();
                    if(ref.isEmpty()){
                        throw new Exception(file.getOriginalFilename() +" Ligne ignorée : ref vide. Ligne " + record.getRecordNumber());
                    }
                    String salaire = record.get("Salaire").trim();
                    if(salaire.isEmpty()){
                        throw new Exception(file.getOriginalFilename() +" Ligne ignorée : salaire vide. Ligne " + record.getRecordNumber());
                    }
                    String valeur = record.get("Salaire Base").trim();
                    if(valeur.isEmpty()){
                        throw new Exception(file.getOriginalFilename() +" Ligne ignorée : valeur vide. Ligne " + record.getRecordNumber());
                    }else{
                        int indexVirgule = valeur.indexOf(',');
                        if(indexVirgule!=-1){
                            StringBuilder sb = new StringBuilder(valeur);
                            sb.setCharAt(indexVirgule, '.');
                            valeur = sb.toString();
                        }
                    }
                    String formattedMois= Util.formatDate(mois);

                    Map<String, Object> salaryStructure = new HashMap<>();
                    salaryStructure.put("Mois", formattedMois);
                    salaryStructure.put("Ref Employe", ref);
                    salaryStructure.put("Salaire Base", valeur);
                    salaryStructure.put("Salaire", salaire);
                    salaryStructures.add(salaryStructure);
                }
                catch (Exception e) {
                    throw new Exception(file.getOriginalFilename() + " Erreur lors de l'analyse de la feuille 3 ligne  " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }

        }catch (IOException e) {
            throw e;
        } catch (Exception exception) {
            throw exception;
        }
        return salaryStructures;
    }
}
