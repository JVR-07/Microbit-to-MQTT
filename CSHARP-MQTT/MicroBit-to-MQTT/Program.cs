using System;
using System.IO.Ports;
using System.Threading;
using MQTTnet;
using MQTTnet.Client;

class Program
{
    private static IMqttClient mqttClient;
    
    static async Task Main(string[] args)
    {
        // === CONFIGURACIÓN ===
        string serialPort = "COMX";   // Cambia X según el puerto
        int baud = 115200;
        
        string brokerIp = "IP-AWS";
        string topic = "lab/3pm25b/microbit/luz";

        // === INICIO MQTT ===
        var factory = new MqttFactory();
        mqttClient = factory.CreateMqttClient();
        
        var options = new MqttClientOptionsBuilder()
            .WithTcpServer(brokerIp, 1883)
            .Build();
            
        await mqttClient.ConnectAsync(options);
        
        Console.WriteLine($"Enviando datos de luz a MQTT ({brokerIp})...\n");

        // === LECTURA SERIAL + PUBLICACIÓN ===
        using (SerialPort ser = new SerialPort(serialPort, baud))
        {
            ser.ReadTimeout = 1000;
            ser.Open();
            Thread.Sleep(2000);
            
            try
            {
                while (true)
                {
                    try
                    {
                        string line = ser.ReadLine().Trim();
                        if (!string.IsNullOrEmpty(line))
                        {
                            int level = int.Parse(line);
                            
                            var message = new MqttApplicationMessageBuilder()
                                .WithTopic(topic)
                                .WithPayload(level.ToString())
                                .Build();
                                
                            await mqttClient.PublishAsync(message);
                            Console.WriteLine($"Publicado: Luz={level}");
                        }
                    }
                    catch (TimeoutException)
                    {
                        // Timeout esperado, continuar leyendo
                    }
                    catch (FormatException)
                    {
                        // Ignorar líneas que no son números
                    }
                }
            }
            catch (OperationCanceledException)
            {
                Console.WriteLine("\nFinalizado por el usuario.");
            }
        }
    }
}