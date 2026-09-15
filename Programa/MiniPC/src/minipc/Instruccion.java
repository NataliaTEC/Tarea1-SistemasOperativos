package minipc;

/**
 * Representa el resultado del analisis de una linea del archivo .asm
 * Esta clase no valida ni procesa nada por si misma, solo guarda el resultado (valido o invalido) que produce {@link ProcesadorInstrucciones}.
 * @author Natalia Granados Rosales 
 */
public class Instruccion {

    private final int numeroLinea;
    private final String lineaOriginal;

    private final Operador operador;
    private final Registro registro;
    private final Integer valor; // null si el operador no usa valor inmediato

    private final String opcodeBinario;
    private final String registroBinario;
    private final String valorBinario;

    private final boolean valida;
    private final String mensajeError;

    // posicion de memoria donde quedara cargada (la asigna el cargador/CPU)
    private int posicionMemoria = -1;

    private Instruccion(int numeroLinea, String lineaOriginal, Operador operador, Registro registro, Integer valor, String opcodeBinario, String registroBinario, String valorBinario, boolean valida, String mensajeError) {
        this.numeroLinea = numeroLinea;
        this.lineaOriginal = lineaOriginal;
        this.operador = operador;
        this.registro = registro;
        this.valor = valor;
        this.opcodeBinario = opcodeBinario;
        this.registroBinario = registroBinario;
        this.valorBinario = valorBinario;
        this.valida = valida;
        this.mensajeError = mensajeError;
    }

    /** Crea una instruccion correctamente analizada y traducida. */
    public static Instruccion crearValida(int numeroLinea, String lineaOriginal, Operador operador, Registro registro, Integer valor, String opcodeBinario, String registroBinario, String valorBinario) {
        return new Instruccion(numeroLinea, lineaOriginal, operador, registro, valor, opcodeBinario, registroBinario, valorBinario, true, null);
    }

    /** Crea una instrucción invalida, guardando el motivo del error. */
    public static Instruccion crearInvalida(int numeroLinea, String lineaOriginal, String mensajeError) {
        return new Instruccion(numeroLinea, lineaOriginal, null, null, null, null, null, null, false, mensajeError);
    }

    public int getNumeroLinea() {
        return numeroLinea;
    }

    public String getLineaOriginal() {
        return lineaOriginal;
    }

    public Operador getOperador() {
        return operador;
    }

    public Registro getRegistro() {
        return registro;
    }

    public Integer getValor() {
        return valor;
    }

    public String getOpcodeBinario() {
        return opcodeBinario;
    }

    public String getRegistroBinario() {
        return registroBinario;
    }

    public String getValorBinario() {
        return valorBinario;
    }

    public boolean isValida() {
        return valida;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public int getPosicionMemoria() {
        return posicionMemoria;
    }

    public void setPosicionMemoria(int posicionMemoria) {
        this.posicionMemoria = posicionMemoria;
    }

    /** Devuelve el binario completo formateado */
    public String getBinarioCompleto() {
        if (!valida) {
            return "";
        }
        return opcodeBinario + " " + registroBinario + " " + valorBinario;
    }

    /** Devuelve el binario completo sin espacios (16 bits), util para guardarlo en memoria. */
    public String getBinarioCompacto() {
        if (!valida) {
            return "";
        }
        return opcodeBinario + registroBinario + valorBinario;
    }

    @Override
    public String toString() {
        if (!valida) {
            return String.format("Linea %d [INVALIDA] \"%s\" -> %s", numeroLinea, lineaOriginal, mensajeError);
        }
        return String.format("Linea %d \"%s\" -> %s (pos:%d)", numeroLinea, lineaOriginal, getBinarioCompleto(), posicionMemoria);
    }
}
