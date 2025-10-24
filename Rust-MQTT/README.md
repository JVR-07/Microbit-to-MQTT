# Introducción
Esta implementación en Rust (1.80 o superior) permite leer los valores de luz enviados por una Micro:bit a través del puerto serial, mostrarlos en consola y publicarlos en un broker MQTT.
  
Se utilizan las librerías:
- `serialport` para la comunicación con el puerto serial.
- `paho-mqtt` para la publicación de datos en el broker MQTT.
- `tokio` para el manejo asíncrono del flujo y tiempos de espera.
# Instalación y Ejecución
1. Instalar Rust y Cargo (usando rustup): https://rust-lang.org/tools/install/
2. Clonar el proyecto:
```bahs
git clone https://github.com/JVR-07/Microbit-to-MQTT/
cd Microbit-to-MQTT/RUST-MQTT
```
3. Configurar dependencias en `Cargo.toml`:
```bash
[package]
name = "microbit_mqtt"
version = "0.1.0"
edition = "2021"

[dependencies]
paho-mqtt = "0.12.1"
serialport = "4.3.0"
tokio = { version = "1.39", features = ["full"] }
ctrlc = "3.4"
```
4. Compilar y ejecutar:
```bash
cargo run
```
# Explicación del Código
El archivo principal `main.rs` se encuentra dentro de `/src` y sigue la siguiente estructura:
## Bibliotecas
```rust
use paho_mqtt as mqtt;
use serialport::SerialPort;
use std::io::{self, BufRead, BufReader};
use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::Arc;
use std::time::Duration;
use tokio::time;
```
Importa las librerías necesarias para:
- MQTT (`paho_mqtt`)
- Comunicación serial (`serialport`)
- Control de hilos y señales (`Arc`, `AtomicBool`, `ctrlc`)
- Operaciones asíncronas y temporización (`tokio::time`)
## Configuración
```rust
const SERIAL_PORT: &str = "/dev/ttyUSBX"; // Cambia X según el puerto
const BAUD_RATE: u32 = 115200;
const BROKER_IP: &str = "IP-AWS";
const TOPIC: &str = "lab/3pm25b/microbit/luz";
```
Define los parámetros de conexión:
- Puerto serial
  - Linux: `/dev/ttyUSB0` o `/dev/ttyACM0`
  - Windows: `COM3, COM4, etc`.
- Baudios: `115200`
- Broker MQTT y topic para la publicación de datos.
## Configuración y Conexión MQTT
```rust
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
```
- Crea el cliente MQTT con `paho-mqtt`.  
- Define las opciones de conexión (`keep-alive`, `clean_session`).
- Se conecta al broker mediante el puerto TCP estándar (`1883`).
## Lectura del Puerto Serial
```rust
let port = serialport::new(SERIAL_PORT, BAUD_RATE)
    .timeout(Duration::from_millis(1000))
    .open()?;

let reader = BufReader::new(port);
```
Abre el puerto serial con un tiempo de espera de 1 segundo y lo envuelve en un `BufReader` para procesar los datos línea por línea.
## Ciclo Principal
```rust
for line in reader.lines() {
    if let Ok(line) = line {
        let trimmed_line = line.trim();
        if let Ok(level) = trimmed_line.parse::<i32>() {
            let msg = mqtt::Message::new(TOPIC, level.to_string(), 0);
            client.publish(msg)?;
            println!("Publicado: Luz={}", level);
        }
    }
    time::sleep(Duration::from_millis(10)).await;
}
```
Cada línea recibida se:
- Limpia de espacios y saltos de línea.
- Convierte a número entero.
- Publica en el topic MQTT especificado.
- Imprime el valor en consola.
## Manejo de Interrupciones
```rust
let running = Arc::new(AtomicBool::new(true));
ctrlc::set_handler(move || {
    running.store(false, Ordering::SeqCst);
})?;
```
Permite detener el programa con `Ctrl + C`, cerrando correctamente la conexión MQTT y liberando recursos.
## Desconexión y Limpieza
```rust
client.disconnect(None)?;
println!("Desconectado de MQTT.");
```
Finaliza la sesión MQTT y muestra un mensaje de salida limpio.
# Conclusiones
La implementación en Rust combina seguridad, concurrencia y rendimiento para crear una aplicación IoT robusta y eficiente.
  
Gracias a las librerías `serialport` y `paho-mqtt`, el programa logra una comunicación estable entre la Micro:bit y el broker MQTT, manteniendo un flujo asíncrono controlado con `Tokio`.
  
El manejo explícito de interrupciones, errores y temporización demuestra la capacidad de Rust para entornos críticos donde la confiabilidad y la eficiencia son esenciales.
  
Es un ejemplo sólido de cómo Rust puede integrarse perfectamente en proyectos IoT modernos, ofreciendo rendimiento de bajo nivel con seguridad de alto nivel.
