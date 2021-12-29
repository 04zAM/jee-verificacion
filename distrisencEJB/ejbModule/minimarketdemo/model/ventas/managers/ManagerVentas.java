package minimarketdemo.model.ventas.managers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import minimarketdemo.model.auditoria.managers.ManagerAuditoria;
import minimarketdemo.model.ventas.dtos.CarritoDTO;
import minimarketdemo.model.core.entities.PrdOrden;
import minimarketdemo.model.core.entities.PrdProducto;
import minimarketdemo.model.core.entities.VenProforma;
import minimarketdemo.model.core.entities.VenDetProforma;
import minimarketdemo.model.core.entities.SegUsuario;
import minimarketdemo.model.core.managers.ManagerDAO;
import minimarketdemo.model.core.utils.ModelUtil;

/**
 * Implementa la logica de manejo de usuarios y autenticacion.
 * Funcionalidades principales:
 * <ul>
 * 	<li>Verificacion de credenciales de usuario.</li>
 *  <li>Asignacion de modulos a un usuario.</li>
 * </ul>
 * @author evasquez
 */
@Stateless
@LocalBean
public class ManagerVentas {
	@EJB
	private ManagerDAO mDAO;
	@EJB
	private ManagerAuditoria mAuditoria;
	
	private CarritoDTO carrito;
    /**
     * Default constructor. 
     */
    public ManagerVentas() {
        carrito = new CarritoDTO();
    }
    
    public List<PrdProducto> findAllProductos(){
    	return mDAO.findAll(PrdProducto.class, "nombre");
    }
    
    public List<VenProforma> findAllProformas(){
    	return mDAO.findAll(VenProforma.class, "idProforma");
    }
    
    public List<VenDetProforma> findCarritoProducts(){
    	return carrito.getListaDetalles();
    }
    
    public void agregarDetalle(PrdProducto nuevoProducto, int cant) throws Exception {
    	VenDetProforma detalle = new VenDetProforma();
    	detalle.setPrdProducto(nuevoProducto);
    	detalle.setCantidad(cant);
    	double total = cant * nuevoProducto.getVenta().doubleValue();
    	detalle.setTotal(new BigDecimal(total));
    	carrito.getListaDetalles().add(detalle);
    	carrito.setTotal(total);
    }
    
    public void eliminarDetalle(VenDetProforma detalle) throws Exception {
    	carrito.getListaDetalles().remove(carrito.getListaDetalles().indexOf(detalle));
    }
    
    public void confirmarPedido(int cliente, String obs) throws Exception {
    	SegUsuario usuario = (SegUsuario) mDAO.findById(SegUsuario.class, cliente);
    	VenProforma proforma = new VenProforma();
    	proforma.setVenDetProformas(carrito.getListaDetalles());
    	proforma.setEstado("Confirmado");
    	proforma.setTotal(new BigDecimal(carrito.getTotal()));
    	proforma.setSegUsuario(usuario);
    	proforma.setFecha(new Date());
    	proforma.setObservaciones(obs);
    	mDAO.insertar(proforma);
    	for (VenDetProforma det : carrito.getListaDetalles()) {
    		det.setVenProforma(proforma);
			mDAO.insertar(det);
		}
    }
    
    public void confirmarPago50(VenProforma proforma, String obs) throws Exception {
    	proforma.setEstado("Pagado 50%");
    	mDAO.actualizar(proforma);
    	PrdOrden orden = new PrdOrden();
    	orden.setEstado("Pendiente");
    	orden.setFecha(new Date());
    	orden.setObservaciones(obs);
    	orden.setVenProforma(proforma);
    	mDAO.insertar(orden);
    }
    
    public void confirmarPagoTotal(VenProforma proforma) throws Exception {
    	proforma.setEstado("Pagado");
    	mDAO.actualizar(proforma);
    }
    
    public void confirmarEntrega(VenProforma proforma) throws Exception {
    	proforma.setEstado("Entregado");
    	mDAO.actualizar(proforma);
    }

}
