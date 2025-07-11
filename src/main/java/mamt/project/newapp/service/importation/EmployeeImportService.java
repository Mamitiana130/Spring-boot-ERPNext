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
public class EmployeeImportService {

    public List<Map<String, Object>> prepareEmployee(MultipartFile file)throws Exception{
        List<Map<String, Object>> employees = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(',')
                     .withFirstRecordAsHeader()
                     .withIgnoreEmptyLines(true)
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                try {
                    String ref = record.get("Ref").trim();
                    if(ref.isEmpty()){
                        throw new Exception(file.getOriginalFilename() +" Ligne ignorée : Ref vide. Ligne " + record.getRecordNumber());
                    }
                    String nom = record.get("Nom").trim();
                    if(nom.isEmpty()){
                        throw new Exception(file.getOriginalFilename() +" Ligne ignorée : nom vide. Ligne " + record.getRecordNumber());
                    }
                    String prenom = record.get("Prenom").trim();
                    if(prenom.isEmpty()){
                        throw new Exception(file.getOriginalFilename() +" Ligne ignorée : prenom vide. Ligne " + record.getRecordNumber());
                    }
                    String dateEmbauche = record.get("Date embauche").trim();
                    if(dateEmbauche.isEmpty()){
                        throw new Exception(file.getOriginalFilename() +" Ligne ignorée : dateEmbauche vide. Ligne " + record.getRecordNumber());
                    }
                    String dateNaissance = record.get("date naissance").trim();
                    if(dateNaissance.isEmpty()){
                        throw new Exception(file.getOriginalFilename() +" Ligne ignorée : dateNaissance vide. Ligne " + record.getRecordNumber());
                    }
                    String company = record.get("company").trim();
                    if(company.isEmpty()){
                        throw new Exception(file.getOriginalFilename() +" Ligne ignorée : company vide. Ligne " + record.getRecordNumber());
                    }
                    String genre = record.get("genre").trim();
                    if(genre.isEmpty()){
                        throw new Exception(file.getOriginalFilename() +" Ligne ignorée : genre vide. Ligne " + record.getRecordNumber());
                    }
                    String birthDate=Util.formatDate(dateNaissance);
                    String joinindDate=Util.formatDate(dateEmbauche);

                    Map<String, Object> employee = new HashMap<>();
                    employee.put("nom", nom);
                    employee.put("prenom", prenom);
                    employee.put("genre", genre);
                    employee.put("Ref", record.get("Ref").trim());
                    employee.put("date naissance", birthDate);
                    employee.put("Date embauche",joinindDate);
                    employee.put("company", company);

                    employees.add(employee);

                }catch (Exception e) {
                    throw new Exception(file.getOriginalFilename() + " Erreur lors de l'analyse de la la feuille 1 ligne " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }

        }catch (IOException e) {
            throw e;
        } catch (Exception exception) {
            throw exception;
        }
        return employees;
    }
}