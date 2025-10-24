import com.fazecast.jSerialComm.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MicrobitSerialReader {
    
    // === CONFIGURACIÓN ===
    private static final String SERIAL_PORT = "COM3"; // Cambia según el puerto
    private static final int BAUD_RATE = 115200;
    
    private static final String BROKER_IP = "IP-AWS";
    private static final String TOPIC = "lab/3pm25b/microbit/luz";
    
    private static MqttClient mqttClient;
    private static boolean running = true;

    public static void main(String[] args) {
        try {
            // === INICIO MQTT ===
            String brokerUrl = "tcp://" + BROKER_IP + ":1883";
            mqttClient = new MqttClient(brokerUrl, MqttClient.generateClientId(), new MemoryPersistence());
            
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setConnectionTimeout(60);
            connOpts.setKeepAliveInterval(60);
            
            mqttClient.connect(connOpts);
            
            System.out.printf("Conectado a MQTT broker: %s%n", BROKER_IP);
            System.out.printf("Enviando datos de luz a MQTT (%s)...%n%n", BROKER_IP);

            // === CONFIGURACIÓN SERIAL ===
            SerialPort serialPort = SerialPort.getCommPort(SERIAL_PORT);
            serialPort.setBaudRate(BAUD_RATE);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(1);
            serialPort.setParity(SerialPort.NO_PARITY);
            
            if (!serialPort.openPort()) {
                System.err.println("Error: No se pudo abrir el puerto serial " + SERIAL_PORT);
                return;
            }
            
            System.out.println("Puerto serial abierto: " + SERIAL_PORT);
            
            // Agregar shutdown hook para cerrar recursos correctamente
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running = false;
                if (serialPort.isOpen()) {
                    serialPort.closePort();
                }
                if (mqttClient != null && mqttClient.isConnected()) {
                    try {
                        mqttClient.disconnect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("\nRecursos liberados.");
            }));

            // === LECTURA SERIAL + PUBLICACIÓN ===
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);
            
            try {
                Thread.sleep(2000); // Espera inicial de 2 segundos
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            StringBuilder buffer = new StringBuilder();
            
            while (running) {
                try {
                    while (serialPort.bytesAvailable() > 0) {
                        byte[] readBuffer = new byte[1];
                        int numRead = serialPort.readBytes(readBuffer, 1);
                        
                        if (numRead > 0) {
                            char c = (char) readBuffer[0];
                            
                            if (c == '\n' || c == '\r') {
                                if (buffer.length() > 0) {
                                    processLine(buffer.toString());
                                    buffer.setLength(0);
                                }
                            } else {
                                buffer.append(c);
                            }
                        }
                    }
                    
                    Thread.sleep(10); // Pequeña pausa para no saturar la CPU
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error en lectura serial: " + e.getMessage());
                    break;
                }
            }
            
            // Cierre de recursos
            serialPort.closePort();
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            
        } catch (MqttException e) {
            System.err.println("Error MQTT: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void processLine(String line) {
        try {
            line = line.trim();
            if (!line.isEmpty()) {
                int level = Integer.parseInt(line);
                
                // Publicar en MQTT
                MqttMessage message = new MqttMessage();
                message.setPayload(String.valueOf(level).getBytes());
                message.setQos(0);
                message.setRetained(false);
                
                mqttClient.publish(TOPIC, message);
                
                System.out.printf("Publicado: Luz=%d%n", level);
            }
        } catch (NumberFormatException e) {
            // Ignorar líneas que no sean números
        } catch (MqttException e) {
            System.err.println("Error publicando en MQTT: " + e.getMessage());
        }
    }
}