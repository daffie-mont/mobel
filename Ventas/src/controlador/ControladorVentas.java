
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.*;
import vista.*;

/**
 *
 * @author EVADAFNEDILIAN
 */

/* COSAS QUE HACER
// Buscar por nombre y que autocomplete (Productos y Ventas)
// Obtener num de venta, fecha, hora
// Obtener el cliente
// Seleccionar el tipo de pago
// Pagar $$
// Obtener el num de empleado (se ocupa sistema completo)
// EXTRA: Ventana emergente para buscar y 1-modificar, 2-cancelar/devolver.
*/


public class ControladorVentas extends DefaultTableCellRenderer implements MouseListener, ActionListener, KeyListener {
    private ModeloVentas modVentas;
    public VistaVentas vistaVentas;
    AgregarProducto vistaAgregarProd = new AgregarProducto(vistaVentas, true);
    Reloj reloj = new Reloj();
    
    public ControladorVentas(ModeloVentas modVentas, VistaVentas vistaVentas) {
        this.modVentas = modVentas;
        this.vistaVentas = vistaVentas;
        this.vistaVentas.btnAgregar.addActionListener(this);
        this.vistaVentas.btnQuitar.addActionListener(this);
        this.vistaVentas.btnCancelar.addActionListener(this);
        //this.vistaVentas.btnBuscar.addActionListener(this);
        this.vistaVentas.btnPagar.addActionListener(this);
        this.vistaVentas.tbDetalleVenta.addMouseListener(this);
        this.vistaVentas.tbDetalleVenta.setDefaultRenderer(Object.class, this);
        
        this.vistaAgregarProd.btnAceptar.addActionListener(this);
        this.vistaAgregarProd.tbProductos.addMouseListener(this);
        this.vistaAgregarProd.tbProductos.setDefaultRenderer(Object.class, this);
        this.vistaAgregarProd.txtBuscarProd.addKeyListener(this);
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
            limpiaCampos();            
        }
        if(vistaVentas.btnBuscar == e.getSource())
        {
            
        }
        if(vistaVentas.btnPagar == e.getSource())
        {
            System.out.println(""+vistaVentas.lblFecha.getText());
            System.out.println(""+vistaVentas.lblHora.getText());
        }
        //Para agregar un producto a la tabla de detalle de venta
        if(vistaAgregarProd.btnAceptar == e.getSource()) {
            enviarProductoTbVenta();
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
    }
    
    public void enviarProductoTbVenta() {
        if(vistaAgregarProd.tbProductos.getRowCount() > 0) {
            String cant = null;
            DefaultTableModel tabladet = (DefaultTableModel) vistaVentas.tbDetalleVenta.getModel();

            String[] dato = new String[5];

            int fila = vistaAgregarProd.tbProductos.getSelectedRow();

            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un registro.");
            } else {
                String idProd = vistaAgregarProd.tbProductos.getValueAt(fila, 0).toString();
                String nombre = vistaAgregarProd.tbProductos.getValueAt(fila, 1).toString();
                String stock = vistaAgregarProd.tbProductos.getValueAt(fila, 2).toString();
                String precio = vistaAgregarProd.tbProductos.getValueAt(fila, 4).toString();
                int c = 0, nuevoStock;
                cant = JOptionPane.showInputDialog(this, "Cantidad:", "Productos", JOptionPane.INFORMATION_MESSAGE);
                /*while (!esNumero(cant) && cant != null) {
                    cant = JOptionPane.showInputDialog(this, "Debe ingresar valores numéricos que sean mayores a 0:",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }*/
                if((cant.equals("")) || (cant.equals("0"))) {
                    JOptionPane.showMessageDialog(this, "Debe ingresar algun valor mayor que 0");
                }
                else if(Integer.parseInt(cant) > Integer.parseInt(stock)) {
                    JOptionPane.showMessageDialog(this, "No hay suficientes productos en el inventario");
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
            JOptionPane.showMessageDialog(this, "No hay registros.");
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        modVentas.buscarProductos(vistaAgregarProd.txtBuscarProd.getText());
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
    
    //Calcula cuánto hay que pagar
    public void calcularTotal() {
        String valorUnitario, cant; 
        double precio;
        int cantidad;
        double subtotal = 0.0, total = 0;
        
        for (int i = 0; i < vistaVentas.tbDetalleVenta.getRowCount(); i++) {
            valorUnitario = vistaVentas.tbDetalleVenta.getValueAt(i, 3).toString();
            cant = vistaVentas.tbDetalleVenta.getValueAt(i, 2).toString();
            precio = Double.parseDouble(valorUnitario);
            cantidad = Integer.parseInt(cant);
            subtotal = precio * cantidad;
            total = total + subtotal;
            vistaVentas.tbDetalleVenta.setValueAt(Math.rint(subtotal * 100) / 100, i, 4);
        }
        vistaVentas.lblTotal.setText("TOTAL A PAGAR: $" + Math.rint(total * 100) / 100);
    }
    
    //Para cuando se cancele una venta
    void limpiaCampos() {
        DefaultTableModel modelo = (DefaultTableModel) this.vistaVentas.tbDetalleVenta.getModel();

        while (modelo.getRowCount() > 0) {
            modelo.removeRow(0);
        }
        vistaVentas.lblTotal.setText("TOTAL A PAGAR: $0.0");
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
