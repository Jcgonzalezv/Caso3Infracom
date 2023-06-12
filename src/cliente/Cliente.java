package cliente;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;

public class Cliente {
	
	private static final String AES = "AES";
	private static final String BLOWFISH = "Blowfish";
	private static final String RSA = "RSA";
	private static final String HMACSHA1 = "HMACSHA1";
	private static final String HMACSHA256 = "HMACSHA256";
	private static final String HMACSHA384 = "HMACSHA384";
	private static final String HMACSHA512 = "HMACSHA512";
	private static final String ERROR = "ERROR";
	private static final String OK = "OK";
	private static Key publicKey;
	private static SecretKey secretKey;
	
	public static final String SERVIDOR = "localhost";
	
	public Cliente(boolean seguridad, int puerto, String id, String algS, String algA, String algH) 
	{
		Socket socket = null;
		PrintWriter pw = null;
		BufferedReader bf = null;

		try {
			socket = new Socket(SERVIDOR, puerto);

			pw =  new PrintWriter(socket.getOutputStream(),true);
			bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));


		}catch(IOException e)
		{
			System.out.println("Error iniciando la conexion con el servidor");
			e.printStackTrace();
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in)); 

		try {
			if(seguridad == true)
			{
				runSeguro(stdIn,bf,pw, algS, algA, algH, id);
			}
			else
			{
				runInseguro(stdIn, bf, pw, algS, algA, algH, id);
			}
			
			stdIn.close();
			pw.close();
			bf.close();
			socket.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
				
		
	}
	
	public static void runSeguro(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut, String algS, String algA, String algH, String pId) throws Exception{
		KeyPair keyPair;
		String algoritmoSimetrico = algS;
		String algoritmoAsimetrico = algA;
		
		pOut.println("HOLA");

		String serverGet="";

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Respuesta del Servidor:" + serverGet);
		}	
		
		if(serverGet.equals(ERROR))
		{
			System.out.println("Error en el servidor");
			return;
		}
			
		
		String rta = "ALGORITMOS:";
		
		rta += algS + ":" + algA + ":" + algH;
		
		pOut.println(rta);

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Respuesta del Servidor:" + serverGet);
		}
		
		if(serverGet.equals(ERROR))
		{
			System.out.println("Error en el servidor");
			return;
		}

		keyPair = generateKey(RSA);
		java.security.cert.X509Certificate certicicate = gc(keyPair);
		byte[] certiBiter = certicicate.getEncoded( );
		String certica3 = Base64.toBase64String(certiBiter);
		
		pOut.println(certica3);

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Respuesta del Servidor:" + serverGet);
		}

		if(serverGet.equals(ERROR))
		{
			System.out.println("Error en el servidor");
			return;
		}

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Recibido certificado del servidor \n desencriptando...");
		}
		
		byte[] serverCert =  Base64.decode(serverGet);
		CertificateFactory cf = CertificateFactory.getInstance("X509");
		InputStream iS =  new ByteArrayInputStream(serverCert);
		certicicate = (X509Certificate)cf.generateCertificate(iS);
		publicKey = certicicate.getPublicKey();

		if(publicKey != null)
		{
			System.out.println("Certificado correcto");
			pOut.println(OK);
		}else {
			System.out.println("Cerificado incorrecto");
			pOut.println(ERROR);
		}

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Recibida llave privada encriptada \n desencriptando...");
		}
		
		if(serverGet.equals(ERROR))
		{
			System.out.println("Error en el servidor");
			return;
		}
		byte[] discover = desifrate((Key)keyPair.getPrivate(), algoritmoAsimetrico, Base64.decode(serverGet));
		secretKey = new SecretKeySpec(discover, 0,discover.length ,algoritmoSimetrico);

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Recibido reto del servidor \n desencriptando...");
		}

		byte[] descifra2 = desifrate(secretKey, algoritmoSimetrico,Base64.decode(serverGet));


		byte[] cifra3 = cifrerAsimetric(publicKey,algoritmoAsimetrico,descifra2);
		String enviarServer = new String(Base64.encode(cifra3));

		System.out.println("Se envia reto cifrado");
		pOut.println(enviarServer);

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Respuesta del Servidor:" + serverGet);
		}
		
		String id = pId;

		byte[] ident = id.getBytes();
		byte[] idUser = cifrerSimetric(secretKey, ident, algS);
		String idUserEnv = DatatypeConverter.printBase64Binary(idUser);
		System.out.println("Se envia identificacion cifrada");
		pOut.println(idUserEnv);

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Hora militar cifrada recibida \n descifrando...");
		}

		byte[] hora = desifrate(secretKey, algoritmoSimetrico,Base64.decode(serverGet));
		
		String horaDescifra3 = DatatypeConverter.printBase64Binary(hora);
		if(!horaDescifra3.isEmpty())
		{
			String hore1 = horaDescifra3.substring(0, (horaDescifra3.length()/2));
			String hoe2 = horaDescifra3.substring((horaDescifra3.length()/2));
			System.out.println("Hora enviada: "+ hore1 + ":" + hoe2);
			
			pOut.println(OK);
			
			System.out.println(OK);
				
		}else {
			pOut.println(ERROR);	
			System.out.println(OK);
		}
	}
	
	public static void runInseguro(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut, String algS, String algA, String algH, String pId) throws Exception{

		String algoritmoSimetrico = algS;
		String algoritmoAsimetrico = algA;
		KeyPair keyPair;
		
		pOut.println("HOLA");

		String serverGet="";

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Respuesta del Servidor:" + serverGet);
		}	
		
		if(serverGet.equals(ERROR))
		{
			System.out.println("Error en el servidor");
			return;
		}
			
		
		String rta = "ALGORITMOS:";
		
		rta += algS + ":" + algA + ":" + algH;
		
		pOut.println(rta);

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Respuesta del Servidor:" + serverGet);
		}
		
		if(serverGet.equals(ERROR))
		{
			System.out.println("Error en el servidor");
			return;
		}

		keyPair = generateKey(RSA);
		java.security.cert.X509Certificate certicicate = gc(keyPair);
		byte[] certiBiter = certicicate.getEncoded( );
		String certica3 = Base64.toBase64String(certiBiter);
		
		pOut.println(certica3);

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Respuesta del Servidor:" + serverGet);
		}

		if(serverGet.equals(ERROR))
		{
			System.out.println("Error en el servidor");
			return;
		}

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Recibido certificado del servidor \n desencriptando...");
		}
		
		byte[] serverCert =  Base64.decode(serverGet);
		CertificateFactory cf = CertificateFactory.getInstance("X509");
		InputStream iS =  new ByteArrayInputStream(serverCert);
		certicicate = (X509Certificate)cf.generateCertificate(iS);
		publicKey = certicicate.getPublicKey();

		if(publicKey != null)
		{
			System.out.println("Certificado correcto");
			pOut.println(OK);
		}else {
			System.out.println("Cerificado incorrecto");
			pOut.println(ERROR);
		}

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Recibida llave privada");
		}
		
		if(serverGet.equals(ERROR))
		{
			System.out.println("Error en el servidor");
			return;
		}
		byte[] discover = serverGet.getBytes();
		secretKey = new SecretKeySpec(discover, 0,discover.length ,algoritmoSimetrico);

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Recibido reto del servidor");
		}

		String enviarServer = serverGet;

		System.out.println("Se envia reto");
		pOut.println(enviarServer);

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Respuesta del Servidor:" + serverGet);
		}
		
		String id = pId;

		pOut.println(id);

		if((serverGet=pIn.readLine())!= null)
		{
			System.out.println("Hora militar recivida");
		}
		
		String horaDescifra3 = serverGet;
		if(!horaDescifra3.isEmpty())
		{
			String hore1 = horaDescifra3.substring(0, (horaDescifra3.length()/2));
			String hoe2 = horaDescifra3.substring((horaDescifra3.length()/2));
			System.out.println("Hora enviada: "+ hore1 + ":" + hoe2);
			
			pOut.println(OK);
			
			System.out.println(OK);
				
		}else {
			pOut.println(ERROR);	
			System.out.println(OK);
		}
	}

	

	public static byte[] cifrerSimetric(SecretKey key, byte[] txt, String algS) {
		byte[] rta;
		
		try {
			Cipher cifrer = Cipher.getInstance(algS);
			
			cifrer.init(Cipher.ENCRYPT_MODE, key);
			rta = cifrer.doFinal(txt);
			
			return rta;
		} catch(Exception e) {
			System.out.println("Error en cifrarSimetrico: "+e.getMessage());
			return null;
		}
	}
	
	public static byte[] cifrerAsimetric(Key key,String alg ,byte[] txt)
	{
		byte[] rta;

		try {
			Cipher cifrer = Cipher.getInstance(alg);
			cifrer.init(Cipher.ENCRYPT_MODE, key);
			rta = cifrer.doFinal(txt);
		}catch (Exception e) {
			System.out.println("Error en cifrarAsimetrico: " + e.getMessage());
			return null;	
		}

		return rta;
	}
	
	public static byte[] desifrate(Key key, String alg, byte[] txt) {

		byte[] rta;

		try {
			Cipher cifrador = Cipher.getInstance(alg);	
			cifrador.init(Cipher.DECRYPT_MODE, key);
			rta = cifrador.doFinal(txt);
		}catch (Exception e){
			System.out.println("Error descifrando: " + e.getMessage());
			return null;
		}

		return rta;
	}

	public static KeyPair generateKey(String alg) throws NoSuchAlgorithmException
	{
		KeyPairGenerator generator = KeyPairGenerator.getInstance(alg);
		generator.initialize(1024);
		return generator.generateKeyPair();

	}

	public static X509Certificate gc(KeyPair keyPair) throws OperatorCreationException, CertificateException
	{
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.add(Calendar.YEAR, 10);

		X509v3CertificateBuilder x509v3CertificateBuilder = new X509v3CertificateBuilder(
				new X500Name("CN=localhost"),
				BigInteger.valueOf(1),
				Calendar.getInstance().getTime(),
				endCalendar.getTime(),
				new X500Name("CN=localhost"),
				SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));

		ContentSigner contentSigner = new JcaContentSignerBuilder("SHA1withRSA").build(keyPair.getPrivate());

		X509CertificateHolder x509CertificateHolder = x509v3CertificateBuilder.build(contentSigner);
		X509Certificate crt = (X509Certificate)(new JcaX509CertificateConverter().setProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()).getCertificate(x509CertificateHolder));
		return crt;
	}



	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, OperatorCreationException, CertificateException, ClassNotFoundException {

		Socket socket = null;
		PrintWriter pw = null;
		BufferedReader bf = null;
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Ingrese tipo de cliente: ");
		System.out.println("Seguro: 1");
		System.out.println("Inseguro: 2");
		int secure = Integer.parseInt(sc.nextLine());
		
		System.out.println("Ingrese el puerto: ");
		int puerto = Integer.parseInt(sc.nextLine());

		System.out.println("Iniciando Cliente en: " + SERVIDOR + ":" + puerto);

		try {
			socket = new Socket(SERVIDOR, puerto);

			pw =  new PrintWriter(socket.getOutputStream(),true);
			bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));


		}catch(IOException e)
		{
			System.out.println("Error iniciando la conexion con el servidor");
			e.printStackTrace();
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Ingrese el argoritmo a usar:");
		System.out.println("Simetrico Blowflish: 1");
		System.out.println("Simetrico AES: 2");
		String algS = "";
		
		int algIngresado = Integer.parseInt(sc.nextLine());
		
		switch (algIngresado) {
		case 1:
			algS = BLOWFISH;
			break;
		case 2:
			algS = AES;
			break;
		default:
			algS = "opcion no valida";
			break;
		}
		System.out.println("Asimetrico RSA: 1");
		String algA = "";
		algIngresado = Integer.parseInt(sc.nextLine());
		
		switch (algIngresado) {
		case 1:
			algA = RSA;
			break;
		default:
			algA = "opcion no valida";
			break;
		}
		
		System.out.println("HmacSHA1: 1");
		System.out.println("HmacSHA256: 2");
		System.out.println("HmacSHA384: 3");
		System.out.println("HmacSHA512: 4");
		String algH = "";
		algIngresado = Integer.parseInt(sc.nextLine());
		switch (algIngresado) 
		{
		case 1:
			algH = HMACSHA1;
			break;
		case 2:
			algH = HMACSHA256;
			break;
		case 3:
			algH = HMACSHA384;
			break;
		case 4:
			algH = HMACSHA512;
			break;
		default:
			algH = "opcion no valida";
			break;
		}
		
		System.out.println("Ingrese su identificación (numero de 4 digitos): ");
		String id = sc.nextLine();
		
		try {
			if(secure == 1)
			{
				runSeguro(stdIn,bf,pw, algS, algA, algH, id);
			}
			else
			{
				runInseguro(stdIn, bf, pw, algS, algA, algH, id);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
				
		stdIn.close();
		pw.close();
		bf.close();
		socket.close();

	}


}



