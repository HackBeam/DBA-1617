/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica2;

import com.eclipsesource.json.*;
import es.upv.dsic.gti_ia.core.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 * Esta clase contiene los atributos y métodos para el comportamiento de nuestro agente 
 * @author Juan José Jiménez García
 */
public class Agente extends SingleAgent {
    
    private static final int NUM_PERCEPCIONES = 100;
    
    private Pair<Integer, Integer> posicion;
    private String percepcion;
    private boolean pisando_objetivo;
    private boolean crashed;
    private int cont_bateria;
    private boolean objetivo_detectado;
    private String server_key;
    private String mundo_elegido;
    private Mapa map;
    private Heuristica heuristic;
    
    /**
     * Constructor de la clase Agente
     * @param aid El ID del agente
     * @param mundo String que indica el mundo al que se va a conectar el agente
     * @throws Exception 
     * @author Juan José Jiménez García
     */
    public Agente(AgentID aid, String mundo) throws Exception {
        
        super(aid);
        this.mundo_elegido = mundo;
    }
    
    /**
     * Inicialización de las variables antes de la ejecución del agente
     * @author Juan José Jiménez García
     */
    @Override
    public void init() {
        
        System.out.println("Inicializando estado del agente...");
        
        this.map = new Mapa();
        this.map.inicializarMapa();
        
        this.heuristic = new Heuristica();
        
        this.objetivo_detectado = false;
        this.pisando_objetivo = false;
        this.crashed = false;
        this.posicion = new Pair(0,0);
        this.percepcion = "";
        this.server_key = "";
        this.cont_bateria = 0;
    }
    
    /**
     * Método que ejecutará el agente cuando se inicie
     * @author Juan José Jiménez García
     */
    @Override
    public void execute() {
        
        System.out.println("Ejecutando el agente...");
        
        // Aquí hay que implementar el funcionamiento del agente y todo el grueso del sistema
        // Este método puede llamar a otros métodos que formen parte del comportamiento
    }
    
    /**
     * Método que se ejecutará cuando el agente vaya a finalizar su ejecución
     * @author Juan José Jiménez García
     */
    @Override
    public void finalize() {
        
        System.out.println("Finalizando ejecución y estado del agente...");
        
		/*
        try {
            // Por ejemplo, aquí se puede hacer la generacion de la imagen png de la traza
            this.procesarTraza();
        } catch (InterruptedException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
		
        super.finalize();
    }
    
	/**
     * Método que se encarga de actualizar los valores del estado del agente
	 * en funcion de la percepcion recibida
	 * 
	 * @param percepcion Cadena JSON recibida desde el servidor
     * @author Gregorio Carvajal Exposito
	 * @throws java.io.IOException
     */
	public void parsearPercepcion(String percepcion) throws IOException {
		if (percepcion.equals("CRASHED")) //Recibido desde un sensor
			crashed = true;
		else {
			JsonObject json = Json.parse(percepcion).asObject();
			List<String> names = json.names();
			
			switch (names.get(0)) {
				case "result":
					String result = json.getString("result", "UNKNOW RESPONSE");
					
					if (!result.equals("OK")) {
						crashed = true;
						System.out.println("ERROR: " + result);
					}
					
					break;
				
				case "gps":
					JsonObject gps = json.get("gps").asObject();
					posicion = new Pair<>(gps.getInt("x", 0), gps.getInt("y", 0));
					
					break;
				
				case "radar":
					JsonArray radar = json.get("radar").asArray();
					int [][] radar_percibido = new int[5][5];
					
					for (int i=0; i<25; i++)
						radar_percibido[i/5][i%5] = radar.get(i).asInt();
					
					map.setRadar(radar_percibido);
					break;
				
				case "scanner":
					//No hace nada en el basico
					break;
					
				case "battery":
					//No se usa por el momento
					break;
				
				case "trace":
					procesarTraza(json);
					break;
			}
			
		}
			
	}
	
    /**
     * Método para el procesamiento de la traza de imagen
     * @author Juan José Jiménez García
	 * @author Gregorio Carvajal Expósito
	 * @param injson Objeto json que contiene la traza
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    public void procesarTraza(JsonObject injson) throws IOException {
        
        try {
            System.out.println("Recibiendo la traza");
            
            //ACLMessage inbox = this.receiveACLMessage();
            //JsonObject injson = Json.parse(inbox.getContent()).asObject();
            JsonArray ja = injson.get("trace").asArray();
            byte data[] = new byte [ja.size()];
            
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) ja.get(i).asInt();
            }
            
            FileOutputStream fos = new FileOutputStream(this.mundo_elegido + " - " + new SimpleDateFormat("yyyy-MM-dd-hh:mm").format(new Date()) + ".png");
            fos.write(data);
            fos.close();
            System.out.println("Traza guardada");
            
        } catch (IOException ex) {
            
            System.err.println("Error procesando la traza");
        }
    }
}