package generator;


import gload.Task;

import java.lang.management.ManagementFactory;
import java.util.Scanner;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import gload.LoadGenerator;

public class Generator
{
	private LoadGenerator generator;

	public Generator(int seconds, boolean secure, int numClient, int puerto, String id, String algS, String algA, String algH) {
		final Task work = this.createTask(secure, puerto, id, algS, algA, algH);
		final int numberOfTasks = numClient;
		final int gapBetweenTasks = seconds;
		(this.generator = new LoadGenerator("Multiple Client Test", numberOfTasks, work, gapBetweenTasks)).generate();
	}

	private Task createTask(boolean secure, int puerto, String id, String algS, String algA, String algH) {
		
		ClientServerTask create = new ClientServerTask();
		create.setup(secure, puerto, id, algS, algA, algH);
		return (Task) create;
	}

	
	public static void main(final String... args) {
		System.out.println("Generador multiplex de clientes");		

		Scanner sc = new Scanner(System.in);
		
		System.out.println("Ingrese seguridad del cliente (debe ser la misma que la del servidor)");
		System.out.println("Seguro: 1");
		System.out.println("Inseguro: 2");
		int sec = Integer.parseInt(sc.nextLine());
		boolean safe = false;
		if(sec == 1)
		{
			safe = true;
		}
		else
		{
			safe = false;
		}
		
		System.out.println("Ingrese el puerto del servidor: ");
		int puerto = Integer.parseInt(sc.nextLine());
		
		System.out.println("Configuración del ambiente ");
		System.out.println("Recuerde digitar los mismos valores ingresados en el servidor ");

		System.out.println("Ingrese el numero de clientes a crear: ");
		int num = Integer.parseInt(sc.nextLine());
		
		System.out.println("Ingrese el tiempo (en milisegundos) entre ejecuciones: ");
		int time = Integer.parseInt(sc.nextLine());
		
		System.out.println("Ingrese id de 4 digitos que va a usar cada cliente ");
		System.out.println("Si no es ingresado incorrectamente la prueba podria no dar resultados");
		String id = sc.nextLine();


		System.out.println("Algoritmo de cifrado cimetrico a usar: ");
		System.out.println("AES: 1");
		System.out.println("Blowfish: 2");
		String algS = "";
		int opcion = Integer.parseInt(sc.nextLine());
		if(opcion == 1) {
			algS = "AES";
		}
		else
		{
			algS = "Blowfish";
		}
		
		String algA = "RSA";
		
		System.out.println("Algoritmo HMACSHA a usar ");
		System.out.println("HMACSHA1: 1");
		System.out.println("HMACSHA256: 2");
		System.out.println("HMACSHA384: 3");
		System.out.println("HMACSHA512: 4");
		
		String algH = "";
		opcion = Integer.parseInt(sc.nextLine());
		
		switch (opcion) {
		case 1:
			algH = "HMACSHA1";
			break;
		case 2:
			algH = "HMACSHA256";
			break;
		case 3:
			algH = "HMACSHA384";
			break;
		case 4:
			algH = "HMACSHA512";
			break;
		default:
			algH = "invalida3";
			break;
		}

		final Generator generator = new Generator(time, safe, num, puerto, id, algS, algA, algH);
	}
}
