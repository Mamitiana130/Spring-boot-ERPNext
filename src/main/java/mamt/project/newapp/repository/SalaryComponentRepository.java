package mamt.project.newapp.repository;

import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SalaryComponentRepository {

    public List<Map<String, Object>> getComponentsByStructure(Connection connection, String structureName)
            throws SQLException {

        List<Map<String, Object>> components = new ArrayList<>();

        String sql = "SELECT sc.name, sc.salary_component, sc.amount, sc.amount_based_on_formula, " +
                "sc.formula, sc.condition, sc.depends_on_payment_days " +
                "FROM `tabSalary Detail` sc " +
                "WHERE sc.parent = ? AND sc.parenttype = 'Salary Structure' " ;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, structureName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> component = new HashMap<>();
                    component.put("name", rs.getString("name"));
                    component.put("salary_component", rs.getString("salary_component"));
                    component.put("amount", rs.getDouble("amount"));
                    component.put("amount_based_on_formula", rs.getBoolean("amount_based_on_formula"));
                    component.put("formula", rs.getString("formula"));
                    component.put("condition", rs.getString("condition"));
                    component.put("depends_on_payment_days", rs.getBoolean("depends_on_payment_days"));
                    components.add(component);
                }
            }
        }
        return components;
    }
}