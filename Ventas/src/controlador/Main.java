
package controlador;

import vista.VistaVentas;
import modelo.ModeloVentas;

/**
 *
 * @author EVADAFNEDILIAN
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ModeloVentas modeloVentas = new ModeloVentas();
        VistaVentas vistaVentas = new VistaVentas();
        ControladorVentas cv = new ControladorVentas(modeloVentas, vistaVentas);
        
        cv.iniciarVista();
    }
    
}
