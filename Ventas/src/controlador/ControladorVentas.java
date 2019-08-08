
package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.*;
import vista.*;

/**
 *
 * @author EVADAFNEDILIAN
 */

/* COSAS QUE HACER
// Buscar por nombre y que autocomplete (Productos y Ventas) LISTO :D
// Obtener num de venta, fecha, hora LISTO :D
// Seleccionar el tipo de pago LISTO :D
// Obtener el cliente ////
// Pagar $$  LISTO :D CREO
// Obtener el num de empleado (se ocupa sistema completo) ////
// EXTRA: Ventana emergente para buscar LISTO :D
    1-modificar ÑO
    2-cancelar/devolver. LISTO :D
*/


public class ControladorVentas implements MouseListener, ActionListener, KeyListener {
    private ModeloVentas modVentas;
    public VistaVentas vistaVentas;
    private double totalPagar;
    AgregarProducto vistaAgregarProd = new AgregarProducto(vistaVentas, true);
    BuscarVentas vistaBuscarProd = new BuscarVentas(vistaVentas, true);
    Reloj reloj = new Reloj();
    
    public ControladorVentas(ModeloVentas modVentas, VistaVentas vistaVentas) {
        this.modVentas = modVentas;
        this.vistaVentas = vistaVentas;
        this.vistaVentas.btnAgregar.addActionListener(this);
        this.vistaVentas.btnQuitar.addActionListener(this);
        this.vistaVentas.btnCancelar.addActionListener(this);
        this.vistaVentas.btnBuscar.addActionListener(this);
        this.vistaVentas.btnPagar.addActionListener(this);
        this.vistaVentas.tbDetalleVenta.addMouseListener(this);
        
        this.vistaAgregarProd.btnAceptar.addActionListener(this);
        this.vistaAgregarProd.tbProductos.addMouseListener(this);
        this.vistaAgregarProd.txtBuscarProd.addKeyListener(this);
        
        this.vistaBuscarProd.btnModificar.addActionListener(this);
        this.vistaBuscarProd.btnEliminar.addActionListener(this);
        this.vistaBuscarProd.tbVentas.addMouseListener(this);
        this.vistaBuscarProd.tbDetalles.addMouseListener(this);
        this.vistaBuscarProd.txtBuscarVenta.addKeyListener(this);        
    }
    
    public void iniciarVista()
    {
        vistaVentas.setTitle("Ventas");
        vistaVentas.pack();
        vistaVentas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        vistaVentas.setLocationRelativeTo(null);
        vistaVentas.tbDetalleVenta.setModel(modVentas.mostrarDetalleVenta(modVentas.obtenerNumVenta()));
        vistaVentas.lblFecha.setText(reloj.fecha());
        vistaVentas.lblNumVenta.setText("No. de venta: "+modVentas.obtenerNumVenta());
        
        vistaVentas.setVisible(true);
        reloj.Horario();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //Abre la ventana de AgregaProducto
        if(vistaVentas.btnAgregar == e.getSource())
        {
            vistaAgregarProd.tbProductos.setModel(modVentas.consultarProductos());
            vistaAgregarProd.setVisible(true);
        }
        //Quita productos de uno por uno
        if(vistaVentas.btnQuitar == e.getSource())
        {
            DefaultTableModel modelo = (DefaultTableModel) this.vistaVentas.tbDetalleVenta.getModel();
            if (modelo.getRowCount() > 0)
            {
                int fila = this.vistaVentas.tbDetalleVenta.getSelectedRow();
                String valorUnitario = this.vistaVentas.tbDetalleVenta.getValueAt(fila, 3).toString();
                String cant = this.vistaVentas.tbDetalleVenta.getValueAt(fila, 2).toString();
                int cantidad = Integer.parseInt(cant);                

                if (this.vistaVentas.tbDetalleVenta.getSelectedRowCount() < 1) {
                    JOptionPane.showMessageDialog(null, "Ups, selecciona un registro");
                }
                else
                {
                    if(cantidad == 1) {
                        modelo.removeRow(fila);
                        calcularTotal();
                    }                
                    else
                    {
                        double precio,total;
                        
                        cantidad--;
                        precio = Double.parseDouble(valorUnitario);
                        total = precio * cantidad;
                        vistaVentas.tbDetalleVenta.setValueAt(String.valueOf(cantidad), fila, 2);
                        vistaVentas.tbDetalleVenta.setValueAt(String.valueOf(total), fila, 4);
                        calcularTotal();                      
                    }
                }                    
            }
        }
        //Cancela una venta
        if(vistaVentas.btnCancelar == e.getSource())
        {
            DefaultTableModel modelo = (DefaultTableModel) this.vistaVentas.tbDetalleVenta.getModel();
            limpiarTabla(modelo);
            vistaVentas.lblTotal.setText("TOTAL A PAGAR: $0.0");
        }
        //Abre la ventana BuscarVentas
        if(vistaVentas.btnBuscar == e.getSource())
        {
            vistaBuscarProd.tbVentas.setModel(modVentas.consultarVentas());
            vistaBuscarProd.setVisible(true);
        }
        
        /////////NO TERMINADA, PARA PAGAR
        if(vistaVentas.btnPagar == e.getSource())
        {
            DefaultTableModel modelo = (DefaultTableModel) this.vistaVentas.tbDetalleVenta.getModel();           
            pagar();
            System.out.println(""+vistaVentas.lblFecha.getText());
            System.out.println(""+vistaVentas.lblHora.getText());
            limpiarTabla(modelo);
            vistaVentas.lblNumVenta.setText("No. de venta: "+modVentas.obtenerNumVenta());
        }
        
        //Para agregar un producto a la tabla de detalle de venta
        if(vistaAgregarProd.btnAceptar == e.getSource()) {
            enviarProductoTbVenta();
        }
        //MODIFICAR una venta ya realizada
        if(vistaBuscarProd.btnModificar == e.getSource()) {
            
        }
        //ELIMINAR una venta ya realizada
        if(vistaBuscarProd.btnEliminar == e.getSource()) {
            if(vistaBuscarProd.tbVentas.getRowCount() > 0) {
                DefaultTableModel modTbVentas = (DefaultTableModel) this.vistaBuscarProd.tbVentas.getModel();
                DefaultTableModel modTbDetalles = (DefaultTableModel) this.vistaBuscarProd.tbDetalles.getModel();
                int fila = vistaBuscarProd.tbVentas.getSelectedRow();
                String numVenta = vistaBuscarProd.tbVentas.getValueAt(fila, 0).toString();
                
                int eliminar = JOptionPane.showConfirmDialog(null, "¿Está seguro de eliminar esta venta?","Aviso",JOptionPane.YES_NO_OPTION);
                if(eliminar == JOptionPane.YES_OPTION){
                    if(modVentas.eliminarVenta(Integer.parseInt(numVenta))) {
                        JOptionPane.showMessageDialog(null, "¡Registro eliminado exitosamente!");
                        modTbVentas.removeRow(fila);
                        limpiarTabla(modTbDetalles);
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Hubo un error al eliminar la venta");
                    }
                }             
            }
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if(vistaAgregarProd.tbProductos == e.getSource())            
        {
            //Doble clic para agregar un producto a la tabla de detalle de venta
            if (e.getClickCount() == 2) {
                enviarProductoTbVenta();
            }
        }
        if(vistaBuscarProd.tbVentas == e.getSource())            
        {
            if (e.getClickCount() == 1) {
                if(vistaBuscarProd.tbVentas.getRowCount() > 0) {
                    int fila = vistaBuscarProd.tbVentas.getSelectedRow();
                    String numVenta = vistaBuscarProd.tbVentas.getValueAt(fila, 0).toString();
                    vistaBuscarProd.lblDetalle.setText("Detalle de venta #"+numVenta);
                    vistaBuscarProd.tbDetalles.setModel(modVentas.consultarDetallesVenta(Integer.parseInt(numVenta)));
                }
            }
            
        }
    }
    
    public void enviarProductoTbVenta() {
        if(vistaAgregarProd.tbProductos.getRowCount() > 0) {
            String cant = null;
            DefaultTableModel tabladet = (DefaultTableModel) vistaVentas.tbDetalleVenta.getModel();

            String[] dato = new String[5];

            int fila = vistaAgregarProd.tbProductos.getSelectedRow();

            if (fila == -1) {
                JOptionPane.showMessageDialog(null, "Seleccione un registro.");
            } else {
                String idProd = vistaAgregarProd.tbProductos.getValueAt(fila, 0).toString();
                String nombre = vistaAgregarProd.tbProductos.getValueAt(fila, 1).toString();
                String stock = vistaAgregarProd.tbProductos.getValueAt(fila, 2).toString();
                String precio = vistaAgregarProd.tbProductos.getValueAt(fila, 4).toString();
                int c = 0, nuevoStock;
                cant = JOptionPane.showInputDialog(null, "Cantidad:", "Productos", JOptionPane.INFORMATION_MESSAGE);
                while (!esNumero(cant) && cant != null) {
                    cant = JOptionPane.showInputDialog(null, "Debe ingresar valores numéricos que sean mayores a 0:",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                if((cant.equals("")) || (cant.equals("0"))) {
                    JOptionPane.showMessageDialog(null, "Debe ingresar algun valor mayor que 0");
                }
                else if(Integer.parseInt(cant) > Integer.parseInt(stock)) {
                    JOptionPane.showMessageDialog(null, "No hay suficientes productos en el inventario");
                }
                else {
                    for (int i = 0; i < vistaVentas.tbDetalleVenta.getRowCount(); i++) {
                        String codigoProd = vistaVentas.tbDetalleVenta.getValueAt(i, 0).toString();
                        String cant1 = vistaVentas.tbDetalleVenta.getValueAt(i, 2).toString();
                        if (idProd.equals(codigoProd)) {
                            int cantT = Integer.parseInt(cant) + Integer.parseInt(cant1);
                            vistaVentas.tbDetalleVenta.setValueAt(String.valueOf(cantT), i, 2);
                            c++;
                            calcularTotal();
                            //Actualizar inventario
                            nuevoStock = Integer.parseInt(stock) - Integer.parseInt(cant);
                            //Actualiza la BD
                            //modVentas.actualizarStockProducto(Integer.parseInt(idProd), nuevoStock);
                            vistaAgregarProd.tbProductos.setValueAt(String.valueOf(nuevoStock), fila, 2);

                        }
                    }
                    //Si el producto no se encuentra en la tabla de ventas, agrega una nueva fila
                    if (c == 0) {
                        dato[0] = idProd;
                        dato[1] = nombre;
                        dato[2] = cant;
                        dato[3] = precio;

                        //Convierte el arreglo en una fila de tabla
                        tabladet.addRow(dato);
                        //Dicha fila la agrega a la ventana principal de ventas
                        vistaVentas.tbDetalleVenta.setModel(tabladet);
                        calcularTotal();
                        nuevoStock = Integer.parseInt(stock) - Integer.parseInt(cant);
                        //Actualiza la BD
                        //modVentas.actualizarStockProducto(Integer.parseInt(idProd), nuevoStock);
                        vistaAgregarProd.tbProductos.setValueAt(String.valueOf(nuevoStock), fila, 2);

                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "No hay registros.");
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        modVentas.buscarProductos(vistaAgregarProd.txtBuscarProd.getText());
        modVentas.buscarVentas(vistaBuscarProd.txtBuscarVenta.getText());
    }
    
    //Calcula cuánto hay que pagar
    public void calcularTotal() {
        String valorUnitario, cant; 
        double precio;
        int cantidad;
        double subtotal, total = 0;
        
        for (int i = 0; i < vistaVentas.tbDetalleVenta.getRowCount(); i++) {
            valorUnitario = vistaVentas.tbDetalleVenta.getValueAt(i, 3).toString();
            cant = vistaVentas.tbDetalleVenta.getValueAt(i, 2).toString();
            precio = Double.parseDouble(valorUnitario);
            cantidad = Integer.parseInt(cant);
            subtotal = precio * cantidad;
            total = total + subtotal;
            vistaVentas.tbDetalleVenta.setValueAt(redondear(subtotal), i, 4);
        }
        vistaVentas.lblTotal.setText("TOTAL A PAGAR: $" + redondear(total));
        this.totalPagar = redondear(total);
    }
    
    public void pagar() {
        String tipoPago;
        int tipo;
        double recibi,cambio;
        Object menu[] = {"1. Efectivo","2. Crédito","3. Cheque"};        
        
        tipoPago = (String) JOptionPane.showInputDialog(null, "Elige el tipo de pago",
                "Tipo de pago", JOptionPane.QUESTION_MESSAGE, null, menu, menu[0]);    
        
        switch(tipoPago)
        {
            case (String) "1. Efectivo":               
                tipo=1;
                recibi = Double.parseDouble(JOptionPane.showInputDialog(null,"Usted debe pagar: $"+this.totalPagar,
                        "Ingrese el importe", JOptionPane.QUESTION_MESSAGE));
                while (recibi < this.totalPagar) {
                    recibi = Double.parseDouble(JOptionPane.showInputDialog(null, "Por favor, ingrese una cantidad igual o mayor al total a pagar:",
                            "Usted debe pagar: $"+this.totalPagar, JOptionPane.ERROR_MESSAGE));
                }                
                cambio = recibi - this.totalPagar;
                JOptionPane.showMessageDialog(null, "Su cambio es: $"+cambio);
                break;
            case (String) "2. Crédito":
                tipo=2;
                //Incremento del 5% por pagar a crédito
                this.totalPagar = this.totalPagar*1.05;
                
                recibi = Double.parseDouble(JOptionPane.showInputDialog(null,"Usted debe pagar: $"+this.totalPagar,
                        "Ingrese el importe", JOptionPane.QUESTION_MESSAGE));
                while (recibi < this.totalPagar) {
                    recibi = Double.parseDouble(JOptionPane.showInputDialog(null, "Por favor, ingrese una cantidad igual o mayor al total a pagar:",
                            "Usted debe pagar: $"+this.totalPagar, JOptionPane.ERROR_MESSAGE));
                }                
                cambio = recibi - this.totalPagar;
                JOptionPane.showMessageDialog(null, "Su cambio es: $"+cambio);
                break;
            case (String) "3. Cheque":
                tipo=3;
                this.totalPagar = this.totalPagar*1.05;
                recibi = Double.parseDouble(JOptionPane.showInputDialog(null,"Usted debe pagar: $"+this.totalPagar,
                        "Ingrese el importe", JOptionPane.QUESTION_MESSAGE));
                while (recibi < this.totalPagar) {
                    recibi = Double.parseDouble(JOptionPane.showInputDialog(null, "Por favor, ingrese una cantidad igual o mayor al total a pagar:",
                            "Usted debe pagar: $"+this.totalPagar, JOptionPane.ERROR_MESSAGE));
                }
                cambio = recibi - this.totalPagar;
                JOptionPane.showMessageDialog(null, "Su cambio es: $"+cambio);               
                break;
        }
    }
    
    //UTILERÍAS
    public double redondear(double numero)
    {
        return Math.rint(numero*1000)/1000;
    }
    
    //Para borrar todos los datos de una tabla
    void limpiarTabla(DefaultTableModel modelo) {
        while (modelo.getRowCount() > 0) {
            modelo.removeRow(0);
        }        
    }
    
    public boolean esNumero(String n) {
        try {
            if(Integer.parseInt(n) > 0) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
    
    //Algunos métodos sobreescritos que no se usan :V
    @Override
    public void mousePressed(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        
    }
    
    
    //CLASE INTERNA PARA EL RELOJ
    public class Reloj implements Runnable
    {
        String hora, minutos, segundos, ampm;
        Thread hilo;
        public void hora()
        {
            Calendar calendario = new GregorianCalendar();
            Date fechaHoraActual = new Date();     
            calendario.setTime(fechaHoraActual);
            ampm = calendario.get(Calendar.AM_PM)==Calendar.AM ?"AM":"PM";
            int h = calendario.get(Calendar.HOUR_OF_DAY);
            int m = calendario.get(Calendar.MINUTE);
            int s = calendario.get(Calendar.SECOND);
            if(ampm.equals("PM"))
            {
                h = h-12;
            } 
            hora = (h>9) ? (""+h) : ("0"+h);
            minutos = (m>9) ? (""+m) : ("0"+m);
            segundos = (s>9) ? (""+s) : ("0"+s);
       }
    
        public void run()
        {
            Thread current=Thread.currentThread();

            while(current==hilo)
            {
                hora();
                vistaVentas.lblHora.setText(hora+":"+minutos+":"+segundos+" "+ampm);
            }

        }

        public String fecha()
        {
            Date fecha=new Date();
            SimpleDateFormat formatoF = new SimpleDateFormat("dd/MM/YYYY");
            return formatoF.format(fecha);
        }
        
        public void Horario(){
            hilo = new Thread(this);
            hilo.start();
        }
    }
}
