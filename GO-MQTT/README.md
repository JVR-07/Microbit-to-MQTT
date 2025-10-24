# Introducción
Esta implementación en Go (Golang) permite leer los datos de luz enviados por una Micro:bit a través del puerto serial, mostrarlos en consola y enviarlos a un broker MQTT.
  
Go se caracteriza por su eficiencia, concurrencia nativa y facilidad para crear aplicaciones rápidas y portables, ideales para entornos IoT.
  
El código usa dos librerías externas:
- `github.com/eclipse/paho.mqtt.golang` → Cliente MQTT.
- `go.bug.st/serial` → Comunicación con el puerto serial.
# Instalación y Ejecución
1. Instalar Go desde: https://go.dev/dl/
2. Clonar el repositorio:
```bash
git clone https://github.com/JVR-07/Microbit-to-MQTT
cd Microbit-to-MQTT/GO-MQTT
```
3. Inicializar el módulo y obtener dependencias:
```bash
go mod tidy
```
4. Ejecutar:
```bash
sudo usermod -a -G dialout $USER
```
# Explicación del Código
El programa principal está en `main.go` y sigue la estructura modular típica de Go.
## Importaciones
```go
import (
	"bufio"
	"fmt"
	"log"
	"os"
	"os/signal"
	"strconv"
	"strings"
	"syscall"
	"time"

	mqtt "github.com/eclipse/paho.mqtt.golang"
	"go.bug.st/serial"
)
```
Incluye paquetes estándar para manejo de archivos, señales, conversión de datos, así como los módulos externos de MQTT y Serial.
## Configuración
```go
const (
	serialPort = "/dev/ttyUSBX" // Cambia X según el puerto
	baudRate   = 115200
	brokerIP   = "IP-AWS"
	topic      = "lab/3pm25b/microbit/luz"
)
```
Define los parámetros de conexión:
- `serialPort`: Puerto serial donde está conectada la Micro:bit.
  - En Linux: `/dev/ttyUSB0` o `/dev/ttyACM0`
  - En Windows: `COM3, COM4, etc`.
- `brokerIP`: Dirección IP del broker MQTT.
- `topic`: Tema donde se publican los valores de luz.
## Conexión al Broker MQTT
```go
opts := mqtt.NewClientOptions()
opts.AddBroker(fmt.Sprintf("tcp://%s:1883", brokerIP))
opts.SetClientID("microbit-reader")

client := mqtt.NewClient(opts)
if token := client.Connect(); token.Wait() && token.Error() != nil {
	log.Fatalf("Error conectando a MQTT: %v", token.Error())
}
```
Configura y conecta el cliente MQTT.  
En caso de error, el programa termina mostrando un mensaje claro.
## Configuración del Puerto Serial
```go
mode := &serial.Mode{
	BaudRate: baudRate,
}
port, err := serial.Open(serialPort, mode)
if err != nil {
	log.Fatalf("Error abriendo puerto serial: %v", err)
}
```
Abre el puerto serial con la velocidad de transmisión definida (`115200` baudios).
Luego se espera brevemente con `time.Sleep(2 * time.Second)` para estabilizar la conexión.
## Manejo de Señales
```go
sigChan := make(chan os.Signal, 1)
signal.Notify(sigChan, os.Interrupt, syscall.SIGTERM)
go func() {
	<-sigChan
	fmt.Println("\nFinalizado por el usuario.")
	port.Close()
	client.Disconnect(250)
	os.Exit(0)
}()
```
Permite detener el programa limpiamente al presionar `Ctrl + C`, cerrando el puerto y desconectando del broker.
## Lectura del Puerto y Publicación
```go
scanner := bufio.NewScanner(port)
for scanner.Scan() {
	line := strings.TrimSpace(scanner.Text())
	if level, err := strconv.Atoi(line); err == nil {
		token := client.Publish(topic, 0, false, fmt.Sprintf("%d", level))
		fmt.Printf("Publicado: Luz=%d\n", level)
	}
}
```
- Se leen las líneas enviadas por la micro:bit.
- Se convierten a enteros (`strconv.Atoi`).
- Se publican en el tema MQTT configurado.
- Cada valor se muestra también en consola.
# Conclusiones
La implementación en Go resalta la simplicidad y eficiencia del lenguaje para proyectos IoT.  
Gracias a su sistema de concurrencia y manejo de errores, se obtiene una aplicación robusta y ligera capaz de ejecutarse en entornos de bajo consumo.
  
El uso de paquetes maduros como `paho.mqtt.golang` y `go.bug.st/serial` facilita el desarrollo de soluciones que combinan hardware y mensajería IoT en tiempo real.  
Este ejemplo demuestra cómo Go puede ser una excelente opción para aplicaciones que requieran rendimiento, portabilidad y estabilidad.
