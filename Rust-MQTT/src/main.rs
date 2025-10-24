use paho_mqtt as mqtt;
use serialport::SerialPort;
use std::io::{self, BufRead, BufReader};
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::Arc;
use std::time::Duration;
use tokio::time;

// Variables globales
const SERIAL_PORT: &str = "/dev/ttyUSBX"; // Cambiar X por el puerto correspondiente
const BAUD_RATE: u32 = 115200;
const BROKER_IP: &str = "IP-AWS";
const TOPIC: &str = "lab/3pm25b/microbit/luz";

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("Iniciando lectura desde Microbit y envío a MQTT...");
    println!("Presiona Ctrl+C para salir.");
    
    // Bandera para controlar el bucle principal
    let running = Arc::new(AtomicBool::new(true));
    let r = running.clone();
    
    // Manejar Ctrl+C
    ctrlc::set_handler(move || {
        r.store(false, Ordering::SeqCst);
    })?;
    
    // Configuración MQTT
    let create_opts = mqtt::CreateOptionsBuilder::new()
        .server_uri(format!("tcp://{}:1883", BROKER_IP))
        .client_id("microbit_reader")
        .finalize();
    
    let client = mqtt::Client::new(create_opts)?;
    
    let conn_opts = mqtt::ConnectOptionsBuilder::new()
        .keep_alive_interval(Duration::from_secs(30))
        .clean_session(true)
        .finalize();
    
    client.connect(conn_opts)?;
    println!("Conectado a MQTT broker: {}", BROKER_IP);
    
    // Configuración puerto serial
    let port = serialport::new(SERIAL_PORT, BAUD_RATE)
        .timeout(Duration::from_millis(1000))
        .open()?;
    
    println!("Conectado al puerto serial: {}", SERIAL_PORT);
    println!("Enviando datos de luz a MQTT...\n");
    
    time::sleep(Duration::from_secs(2)).await;
    
    // Bucle principal
    let reader = BufReader::new(port);
    
    for line in reader.lines() {
        if !running.load(Ordering::SeqCst) {
            println!("\nFinalizado por el usuario.");
            break;
        }
        
        match line {
            Ok(line) => {
                let trimmed_line = line.trim();
                if !trimmed_line.is_empty() {
                    if let Ok(level) = trimmed_line.parse::<i32>() {
                        let msg = mqtt::Message::new(TOPIC, level.to_string(), 0);
                        
                        if let Err(e) = client.publish(msg) {
                            eprintln!("Error publicando MQTT: {}", e);
                        } else {
                            println!("Publicado: Luz={}", level);
                        }
                    }
                }
            }
            Err(e) if e.kind() == io::ErrorKind::TimedOut => {
                continue;
            }
            Err(e) => {
                eprintln!("Error leyendo puerto serial: {}", e);
                break;
            }
        }
        
        time::sleep(Duration::from_millis(10)).await;
    }
    
    // Desconectar MQTT
    client.disconnect(None)?;
    println!("Desconectado de MQTT.");
    
    Ok(())
}