/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import DB.DBOperations;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author PK
 */
public class JDBCUtil 
{
    private final String JNDINAME = "JNDI_SAPSAN";
    
    public DBOperations getDBOperations ()
    {
        try 
        {
            return new DBOperations(new InitialContext(), JNDINAME);
        } 
        catch (NamingException ex) 
        {
            Logger.getLogger("LOG_DB_".concat(JNDINAME)).log(Level.SEVERE, null, ex);
            return null;
        }
    }               
}
