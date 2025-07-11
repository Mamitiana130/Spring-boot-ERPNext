package mamt.project.newapp.service.importation;

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
public class SalaryStructureImportService {
    public List<Map<String, Object>> prepareSalaryStructure(MultipartFile file) throws Exception {
        List<Map<String, Object>> salaryStructures = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(',')
                     .withFirstRecordAsHeader()
                     .withIgnoreEmptyLines(true)
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                try {
                    Map<String, Object> salaryStructure = new HashMap<>();

                    String salaryStructureName = record.get("salary structure").trim();
                    if (salaryStructureName.isEmpty()) {
                        throw new Exception("Salary structure vide. Ligne " + record.getRecordNumber());
                    }
                    salaryStructure.put("salary structure", salaryStructureName);

                    String name = record.get("name").trim();
                    if (name.isEmpty()) {
                        throw new Exception("Nom de composant vide. Ligne " + record.getRecordNumber());
                    }
                    salaryStructure.put("name", name);

                    String abbr = record.get("Abbr").trim();
                    if (abbr.isEmpty()) {
                        throw new Exception("Abbr vide. Ligne " + record.getRecordNumber());
                    }
                    salaryStructure.put("Abbr", abbr);

                    String type = record.get("type").trim();
                    if (type.isEmpty() || (!type.equalsIgnoreCase("earning") && !type.equalsIgnoreCase("deduction"))) {
                        throw new Exception("Type de composant invalide ou vide. Doit être 'earning' ou 'deduction'. Ligne " + record.getRecordNumber());
                    }
                    salaryStructure.put("type", type);

                    String valeur = record.get("valeur").trim();
                    if (valeur.isEmpty()) {
                        valeur = "base";
                    } else {
                        valeur = valeur.replace(',', '.');
                    }
                    salaryStructure.put("valeur", valeur);

                    String company = record.get("company").trim();
                    if (company.isEmpty()) {
                        throw new Exception("Nom de l'entreprise vide. Ligne " + record.getRecordNumber());
                    }
                    salaryStructure.put("company", company);

                    salaryStructures.add(salaryStructure);

                } catch (Exception e) {
                    throw new Exception(file.getOriginalFilename() + " Erreur à la ligne " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new IOException("Erreur de lecture du fichier CSV : " + e.getMessage(), e);
        } catch (Exception e) {
            throw e;
        }
        return salaryStructures;
    }
}
