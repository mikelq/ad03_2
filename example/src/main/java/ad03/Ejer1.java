package ad03;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Ejer1 {

    public static void main(String[] args) {

        String sql = 
        		"SELECT c.id_curso, "
        		           + "       c.titulo, "
        		           + "       c.fecha, "
        		           + "       COUNT(m.dni) AS matriculados, "
        		           + "       a.capacidad, "
        		           + "       a.nombre AS aula, "
        		           + "       a.edificio "
        		           + "FROM cursos c "
        		           + "JOIN aulas a ON a.id_aula = c.id_aula "
        		           + "LEFT JOIN matriculas m ON m.id_curso = c.id_curso "
        		           + "GROUP BY c.id_curso, c.titulo, c.fecha, a.capacidad, a.nombre, a.edificio "
        		           + "ORDER BY c.fecha, c.id_curso";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.printf(
                    "%-4s | %-30s | %-10s | %-12s | %-9s | %-28s | %s%n",
                    "ID", "Título", "Fecha", "Matriculados",
                    "Capacidad", "Aula", "Edificio");

            System.out.println("----------------------------------------------------------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf(
                        "%-4d | %-30s | %-10s | %-12d | %-9d | %-28s | %s%n",
                        rs.getInt("id_curso"),
                        rs.getString("titulo"),
                        rs.getDate("fecha"),
                        rs.getInt("matriculados"),
                        rs.getInt("capacidad"),
                        rs.getString("aula"),
                        rs.getString("edificio"));
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar los cursos: "
                    + e.getMessage());
        }
    }
}
