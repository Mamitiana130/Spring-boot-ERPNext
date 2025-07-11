package mamt.project.newapp.repository;

import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SalaryStructureRepository {

    public List<Map<String, Object>> getAllSalaryStructures(Connection connection) throws SQLException {
        List<Map<String, Object>> structures = new ArrayList<>();
        String sql = "SELECT name, company, currency, payroll_frequency " +
                "FROM `tabSalary Structure` WHERE docstatus = 1";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> structure = new HashMap<>();
                structure.put("name", rs.getString("name"));
                structure.put("company", rs.getString("company"));
                structure.put("currency", rs.getString("currency"));
                structure.put("payroll_frequency", rs.getString("payroll_frequency"));
                structures.add(structure);
            }
        }
        return structures;
    }
    public Map<String, Object> getByName(Connection connection, String name) throws SQLException {
        String sql = "SELECT name, company, currency, payroll_frequency " +
                "FROM `tabSalary Structure` WHERE name = ? AND docstatus = 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> structure = new HashMap<>();
                    structure.put("name", rs.getString("name"));
                    structure.put("company", rs.getString("company"));
                    structure.put("currency", rs.getString("currency"));
                    structure.put("payroll_frequency", rs.getString("payroll_frequency"));
                    return structure;
                }
            }
        }
        return null;
    }
    public void insertSalaryStructure(Connection connection, String name, String company, String currency, String payrollFrequency) throws SQLException {
        String sql = "INSERT INTO `tabSalary Structure` " +
                "(name, company, currency, payroll_frequency, docstatus, creation, modified,modified_by) " +
                "VALUES (?, ?, ?, ?, 1, ?, ?,?)";

        Timestamp now = new Timestamp(System.currentTimeMillis());

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, company);
            stmt.setString(3, currency);
            stmt.setString(4, payrollFrequency);
            stmt.setTimestamp(5, now); // creation
            stmt.setTimestamp(6, now); // modified
            stmt.setString(7, "Administrator");
            stmt.executeUpdate();
        }
    }

    public void updateSalaryStructure(Connection connection, String name, String company, String currency, String payrollFrequency) throws SQLException {
        String sql = "UPDATE `tabSalary Structure` SET company = ?, currency = ?, payroll_frequency = ?, modified = ? " +
                "WHERE name = ? AND docstatus = 1";

        Timestamp now = new Timestamp(System.currentTimeMillis());

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, company);
            stmt.setString(2, currency);
            stmt.setString(3, payrollFrequency);
            stmt.setTimestamp(4, now); // nouvelle date de modification
            stmt.setString(5, name);
            stmt.executeUpdate();
        }
    }

    public void updateDateEmployee(Connection connection, String employee, String startDate) throws SQLException {
        String sql = "UPDATE `tabSalary Slip` SET docstatus = ?, status = ?" +
                "WHERE employee = ? AND docstatus = 1 AND start_date = ?";


        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, 2);
            stmt.setString(2, "Cancel");
            stmt.setString(3, employee);
            stmt.setString(4, startDate);

            stmt.executeUpdate();
        }
    }

}