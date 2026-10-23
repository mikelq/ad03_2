package ad03;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Scanner;

public class Ejer4 {

    public static void main(String[] args) {

        try (Scanner teclado = new Scanner(System.in);
             Connection con = ConexionBD.obtenerConexion()) {

            // Muestra los cursos disponibles
            String sql = """
                    SELECT id_curso, titulo, fecha
                    FROM cursos
                    """;

            System.out.println("Listado de cursos:");

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    System.out.printf(
                    		" %d.  %-25s%n",
                            rs.getInt("id_curso"),
                            rs.getString("titulo"));
                }
            }

            System.out.print(
                    "\nIntroduce el ID del curso: ");

            int idCurso;

            try {
                idCurso = Integer.parseInt(teclado.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("El ID debe ser un número.");
                return;
            }

            // Llamamos a la función almacenada de MySQL
            String llamada =
                    "{? = call obtener_numero_matriculados(?,?)}";

            try (CallableStatement cs =
                         con.prepareCall(llamada)) {

                cs.registerOutParameter(1, Types.INTEGER);
                cs.setInt(2, idCurso);
                cs.execute();

                int total = cs.getInt(1);

                if (cs.wasNull()) {
                    System.out.println(
                            "La función ha devuelto NULL.");
                } else {
                    System.out.println(
                            "Número dealumnos matriculados: " + total);
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error:" + e.getMessage());
        }
    }
}
