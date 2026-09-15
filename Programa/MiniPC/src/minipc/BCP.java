package minipc;

import java.util.EnumMap;
import java.util.Map;

/**
 * Bloque de Control de Proceso (BCP / PCB).
 *
 * Guarda todo el estado necesario para ejecutar (o pausar/retomar) un proceso en la Mini PC:
 *   - Identificacion y estado del proceso.
 *   - Registros de la CPU: PC, IR (opcode+registro y valor recién leídos), AC.
 *   - Registros de proposito general: AX, BX, CX, DX (valores reales con signo).
 *   - Limites de memoria asignados al proceso (protección).
 * @author Natalia Granados Rosales
 */
public class BCP {

    private final int pid;
    private EstadoProceso estado;

    // Campos informativos adicionales para mostrar en la interfaz
    private int prioridad;
    private int posicionBCP;

    // Contador de programa: posición de memoria de la proxima instruccion a leer.
    private int pc;

    // Registro de instrucción: guarda tanto el código crudo leído de memoria como su forma legible en ensamblador  que es la que se muestra normalmente.
    private int irCodigo;
    private String ir;

    // Acumulador: almacenamiento temporal para operaciones (valor real, con signo).
    private int ac;

    // Registros de proposito general (valor real, con signo).
    private final Map<Registro, Integer> registros;

    private final int limiteInferior;
    private final int limiteSuperior;

    private int instruccionesEjecutadas;

    public BCP(int pid, int limiteInferior, int limiteSuperior) {
        this.pid = pid;
        this.limiteInferior = limiteInferior;
        this.limiteSuperior = limiteSuperior;

        this.estado = EstadoProceso.NUEVO;
        this.pc = limiteInferior;
        this.irCodigo = 0;
        this.ir = ""; // aún no se ha leído ninguna instrucción
        this.ac = 0;
        this.instruccionesEjecutadas = 0;
        this.prioridad = 1;
        this.posicionBCP = 0;

        this.registros = new EnumMap<>(Registro.class);
        for (Registro registro : Registro.values()) {
            registros.put(registro, 0);
        }
    }

    // Operaciones de uso

    /** Avanza el PC una posicion (cada instruccion ocupa 1 palabra de memoria). */
    public void avanzarPC() {
        this.pc += 1;
    }

    /** Registra que se ejecuto una instruccion mas. */
    public void registrarInstruccionEjecutada() {
        this.instruccionesEjecutadas++;
    }

    /** Verifica si el PC sigue dentro del espacio de memoria asignado a este proceso. */
    public boolean tieneInstruccionesPendientes() {
        return pc <= limiteSuperior && estado != EstadoProceso.TERMINADO;
    }

    public int obtenerValorRegistro(Registro registro) {
        return registros.get(registro);
    }

    public void asignarValorRegistro(Registro registro, int valor) {
        registros.put(registro, valor);
    }

    // ------------------------------------------------------------------
    // Getters / Setters
    // ------------------------------------------------------------------

    public int getPid() {
        return pid;
    }

    public EstadoProceso getEstado() {
        return estado;
    }

    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
    }

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public int getIrCodigo() {
        return irCodigo;
    }

    public void setIrCodigo(int irCodigo) {
        this.irCodigo = irCodigo;
    }

    public String getIr() {
        return ir;
    }

    public void setIr(String ir) {
        this.ir = ir;
    }

    public int getAc() {
        return ac;
    }

    public void setAc(int ac) {
        this.ac = ac;
    }

    public int getLimiteInferior() {
        return limiteInferior;
    }

    public int getLimiteSuperior() {
        return limiteSuperior;
    }

    public int getInstruccionesEjecutadas() {
        return instruccionesEjecutadas;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public int getPosicionBCP() {
        return posicionBCP;
    }

    public void setPosicionBCP(int posicionBCP) {
        this.posicionBCP = posicionBCP;
    }

    /** Devuelve el IR (codigo crudo) en binario de 16 bits*/
    public String getIrComoBinario() {
        return Memoria.palabraABinario(irCodigo);
    }

    @Override
    public String toString() {
        return String.format( "BCP[pid=%d, estado=%s, PC=%d, IR=%s, AC=%d, AX=%d, BX=%d, CX=%d, DX=%d, instrucciones=%d]", pid, estado, pc, ir, ac, registros.get(Registro.AX), registros.get(Registro.BX), registros.get(Registro.CX), registros.get(Registro.DX), instruccionesEjecutadas);
    }
}