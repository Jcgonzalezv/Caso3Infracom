package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;



public class C {
	private static ServerSocket ss;	
	private static final String MAESTRO = "MAESTRO: ";
	private static X509Certificate certSer; /* acceso default */
	private static KeyPair keyPairServidor; /* acceso default */
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		
		System.out.println(MAESTRO + "Establezca seguridad del servidor:");
		System.out.println("1. Seguro ");
		System.out.println("2. Inseguro ");
		int security = Integer.parseInt(br.readLine());
		boolean seguridad = false;
		if(security == 1)
			seguridad = true;
		String palLog = "";
		if(seguridad == true)
		{
			palLog = "seguro";
		}
		else
		{
			palLog = "inseguro";
		}
		
		System.out.println(MAESTRO + "Establezca puerto de conexion:");	
		int ip = Integer.parseInt(br.readLine());
		
		System.out.println("Ingrese el tamanio del pool de threads: ");
		int numThreads = Integer.parseInt(br.readLine());
		
		System.out.println("Ingrese el numero de clientes del escenario: ");
		int clients = Integer.parseInt(br.readLine());
		
		System.out.println("Ingrese el tiempo (en milis) entre transacciones del escenario: ");
		int time = Integer.parseInt(br.readLine());
		
		System.out.println("Ingrese la repetición a realizar (num del 1 - 10): ");
		int repeticion = Integer.parseInt(br.readLine());
		
		System.out.println(MAESTRO + "Empezando servidor maestro en puerto " + ip);

		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());		

		File file = null;
		keyPairServidor = S.grsa();
		certSer = S.gc(keyPairServidor); 
		String ruta = "./data/logs/" + palLog + "/Pool-" + numThreads + "-Clientes-" + clients + "-Tiempo-" + time + "-Rept-" + repeticion + ".txt";
		   
        file = new File(ruta);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        fw.close();
        
        File fileCSV = null;
		keyPairServidor = S.grsa();
		certSer = S.gc(keyPairServidor);
		String rutaCSV = "./data/experimentos/" + palLog + "/Pool-" + numThreads + "-Clientes-" + clients + "-Tiempo-" + time + "-Rept-" + repeticion + ".csv";

		fileCSV = new File(rutaCSV);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fwer = new FileWriter(fileCSV);
		fwer.write("Server;Tiempo Total Trx(milis);Porcentaje Carga CPU Inicio;Porcentaje Carga CPU Final;Estado de Finalizacion Trx \n");
		fwer.close();

        D.init(certSer, keyPairServidor,file, fileCSV, seguridad);
        
		ss = new ServerSocket(ip);
		System.out.println(MAESTRO + "Socket creado.");
		
		ThreadPoolExecutor threats = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
        
		for (int i=0;true;i++) {
			try { 
				Socket sc = ss.accept();
				System.out.println(MAESTRO + "Cliente " + i + " aceptado.");
				D d = new D(sc,i);
				threats.execute(d);
			} catch (IOException e) {
				System.out.println(MAESTRO + "Error creando el socket cliente.");
				e.printStackTrace();
			}
		}
	}
}
