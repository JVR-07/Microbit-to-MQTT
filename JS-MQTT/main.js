const { SerialPort } = require('serialport');
const { ReadlineParser } = require('@serialport/parser-readline');
const mqtt = require('mqtt');

// === CONFIGURACIÓN ===
const SERIAL_PORT = '/dev/ttyUSBX';  // Cambia X según el puerto
const BAUD_RATE = 115200;

const BROKER_IP = 'IP-AWS';
const TOPIC = 'lab/3pm25b/microbit/luz';

async function main() {
    try {
        // === INICIO MQTT ===
        const client = mqtt.connect(`mqtt://${BROKER_IP}:1883`);
        
        await new Promise((resolve, reject) => {
            client.on('connect', resolve);
            client.on('error', reject);
        });

        console.log(`Conectado a MQTT broker (${BROKER_IP})`);
        console.log(`Enviando datos de luz a MQTT...\n`);

        // === LECTURA SERIAL ===
        const port = new SerialPort({ path: SERIAL_PORT, baudRate: BAUD_RATE });
        const parser = port.pipe(new ReadlineParser({ delimiter: '\n' }));

        parser.on('data', (data) => {
            try {
                const line = data.trim();
                if (line) {
                    const level = parseInt(line);
                    
                    // Publicar en MQTT
                    client.publish(TOPIC, level.toString(), (err) => {
                        if (err) {
                            console.error('Error publicando en MQTT:', err);
                        }
                    });
                    
                    console.log(`Publicado: Luz=${level}`);
                }
            } catch (error) {
                // Ignorar errores de conversión
                if (error.name !== 'SyntaxError') {
                    console.error('Error procesando datos:', error);
                }
            }
        });

        port.on('error', (err) => {
            console.error('Error en puerto serial:', err.message);
        });

        port.on('open', () => {
            console.log(`Puerto serial ${SERIAL_PORT} abierto correctamente`);
        });

        // Manejo de cierre elegante
        process.on('SIGINT', async () => {
            console.log('\nFinalizando...');
            client.end();
            port.close();
            process.exit(0);
        });

    } catch (error) {
        console.error('Error inicializando:', error);
        process.exit(1);
    }
}

main();