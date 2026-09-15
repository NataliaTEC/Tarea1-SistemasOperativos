package minipc;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementa el ciclo de ejecucion de la Mini PC: FETCH -> DECODE -> EXECUTE.
 * 
 * Soporta ejecutar un solo paso (botón "Paso a paso") o el programa completo de corrido (botón "Ejecutar").
 * @author Natalia Granados Rosales
 */
public class CPU {

    private final Memoria memoria;

    public CPU(Memoria memoria) {
        this.memoria = memoria;
    }

    // ------------------------------------------------------------------
    // FETCH
    // ------------------------------------------------------------------

    /** Lee de Memoria la palabra (16 bits) apuntada por el PC y la guarda en el IR del BCP. */
    private void fetch(BCP bcp) {
        int pcActual = bcp.getPc();

        if (!memoria.esPosicionValida(pcActual)) {
            throw new IllegalStateException(
                    "El PC (" + pcActual + ") quedó fuera del rango válido de memoria.");
        }

        int codigo = memoria.leer(pcActual);
        bcp.setIrCodigo(codigo);
    }

    // ------------------------------------------------------------------
    // DECODE
    // ------------------------------------------------------------------

    /**
     * Interpreta lo que quedo en el IR del BCP (una palabra de 16 bits) y reconstruye una Instruccion (operador, registro y si aplica el valor
     * inmediato ya interpretado con signo). Tambien actualiza el IR del BCP con la forma legible en ensamblador, que es la que se muestra normalmente en la interfaz. 
     * El número de línea original no se conoce en esta etapa, por lo que se usa -1.
     */
    private Instruccion decode(BCP bcp) {
        String palabraBinaria = Memoria.palabraABinario(bcp.getIrCodigo());
        String opcodeBits = palabraBinaria.substring(0, 4);
        String registroBits = palabraBinaria.substring(4, 8);
        String valorBits = palabraBinaria.substring(8, 16);

        Operador operador = Operador.fromCodigoBinario(opcodeBits);
        if (operador == null) {
            throw new IllegalStateException("Opcode no reconocido en memoria: " + opcodeBits);
        }

        Registro registro = Registro.fromCodigoBinario(registroBits);
        if (registro == null) {
            throw new IllegalStateException("Registro no reconocido en memoria: " + registroBits);
        }

        Integer valor = null;
        if (operador.requiereValorInmediato()) {
            int valorByte = Memoria.binarioAEnteroGeneral(valorBits);
            valor = Memoria.byteAEnteroConSigno(valorByte);
        }

        String mnemonico = operador.name() + " " + registro.name() + (valor != null ? ", " + valor : "");
        bcp.setIr(mnemonico);

        return Instruccion.crearValida(-1, mnemonico, operador, registro, valor,
                opcodeBits, registroBits, valorBits);
    }

    // ------------------------------------------------------------------
    // EXECUTE
    // ------------------------------------------------------------------

    /** Aplica la operacion decodificada sobre el BCP, actualizando AC y los registros. */
    private void execute(BCP bcp, Instruccion instruccion) {
        Registro registro = instruccion.getRegistro();

        switch (instruccion.getOperador()) {
            case MOV:
                bcp.asignarValorRegistro(registro, instruccion.getValor());
                break;

            case LOAD:
                bcp.setAc(bcp.obtenerValorRegistro(registro));
                break;

            case STORE:
                bcp.asignarValorRegistro(registro, bcp.getAc());
                break;

            case ADD: {
                int resultado = bcp.getAc() + bcp.obtenerValorRegistro(registro);
                validarRango(resultado);
                bcp.setAc(resultado);
                break;
            }

            case SUB: {
                int resultado = bcp.getAc() - bcp.obtenerValorRegistro(registro);
                validarRango(resultado);
                bcp.setAc(resultado);
                break;
            }

            default:
                throw new IllegalStateException("Operador no soportado: " + instruccion.getOperador());
        }
    }

    /** El AC y los registros solo pueden guardar valores representables en el formato de 8 bits (-127 a 127). */
    private void validarRango(int valor) {
        if (Math.abs(valor) > 127) {
            throw new ArithmeticException("Desbordamiento: el resultado (" + valor + ") excede el rango permitido (-127 a 127).");
        }
    }

    // ------------------------------------------------------------------
    // Control de ejecución (paso a paso / completo)
    // ------------------------------------------------------------------

    /**
     * Ejecuta un solo paso del ciclo fetch-decode-execute sobre el proceso indicado.
     * @return la Instruccion que se acaba de ejecutar, o null si el proceso ya no tenía instrucciones pendientes (ya termino).
     */
    public Instruccion ejecutarUnPaso(BCP bcp) {
        if (!bcp.tieneInstruccionesPendientes()) {
            bcp.setEstado(EstadoProceso.TERMINADO);
            return null;
        }

        bcp.setEstado(EstadoProceso.EJECUTANDO);

        fetch(bcp);
        Instruccion instruccion = decode(bcp);
        execute(bcp, instruccion);

        bcp.avanzarPC();
        bcp.registrarInstruccionEjecutada();

        if (!bcp.tieneInstruccionesPendientes()) {
            bcp.setEstado(EstadoProceso.TERMINADO);
        }
        return instruccion;
    }

    /**
     * Ejecuta el programa completo de corrido, paso por paso internamente, hasta que el proceso termine.
     * @return la lista de instrucciones ejecutadas, en orden.
     */
    public List<Instruccion> ejecutarTodo(BCP bcp) {
        List<Instruccion> ejecutadas = new ArrayList<>();
        Instruccion instruccion;
        while ((instruccion = ejecutarUnPaso(bcp)) != null) {
            ejecutadas.add(instruccion);
        }
        return ejecutadas;
    }
}