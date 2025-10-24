package main

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

// Configuración
const (
	serialPort = "/dev/ttyUSBX" // Cambia X según el puerto
	baudRate   = 115200
	brokerIP   = "IP-AWS"
	topic      = "lab/3pm25b/microbit/luz"
)

func main() {
	// Configurar MQTT
	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("tcp://%s:1883", brokerIP))
	opts.SetClientID("microbit-reader")

	client := mqtt.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		log.Fatalf("Error conectando a MQTT: %v", token.Error())
	}
	defer client.Disconnect(250)

	fmt.Printf("Conectado a MQTT (%s). Enviando datos de luz...\n\n", brokerIP)

	// Configurar puerto serial
	mode := &serial.Mode{
		BaudRate: baudRate,
	}

	port, err := serial.Open(serialPort, mode)
	if err != nil {
		log.Fatalf("Error abriendo puerto serial: %v", err)
	}
	defer port.Close()

	// Esperar a que se estabilice la conexión serial
	time.Sleep(2 * time.Second)

	// Manejar interrupción (Ctrl+C)
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, os.Interrupt, syscall.SIGTERM)
	go func() {
		<-sigChan
		fmt.Println("\nFinalizado por el usuario.")
		port.Close()
		client.Disconnect(250)
		os.Exit(0)
	}()

	// Leer datos del puerto serial
	scanner := bufio.NewScanner(port)
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if line == "" {
			continue
		}

		// Convertir a entero y publicar
		if level, err := strconv.Atoi(line); err == nil {
			// Publicar via MQTT
			token := client.Publish(topic, 0, false, fmt.Sprintf("%d", level))
			if token.Wait() && token.Error() != nil {
				log.Printf("Error publicando MQTT: %v", token.Error())
				continue
			}

			fmt.Printf("Publicado: Luz=%d\n", level)
		}
		// Ignorar líneas que no se pueden convertir a entero (como en Python)
	}

	if err := scanner.Err(); err != nil {
		log.Printf("Error leyendo del puerto serial: %v", err)
	}
}