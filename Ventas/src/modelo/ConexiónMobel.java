
package modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Super Daf
 */
public class ConexiónMobel 
{
    public Connection abrirConexion() throws SQLException
    {
        Connection con;
        try
        {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mobel","root","");
        }
        catch(SQLException e)
        {
            System.out.println("No se pudo abrir conexión");
            con = null;
        }            
        return con;
    }    
    public void cerrarConexion(Connection c) throws SQLException
    {        
        try
        {
            if(!c.isClosed())
            {
                c.close();
            }
        }
        catch(SQLException e)
        {
            System.out.println("Error al cerrar la conexión");
        }        
    }
}
