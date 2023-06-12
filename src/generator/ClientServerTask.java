package generator;

import cliente.Cliente;
import gload.Task;

public class ClientServerTask extends Task
{
	private int puerto;

	private String cedula;

	private String algS;

	private String algA;

	private String algH;
	
	private boolean seguro;

	public void setup(boolean seguridad, int puerto, String id, String algS, String algA, String algH) {
		this.puerto = puerto;
		this.cedula = id;
		this.algS = algS;
		this.algA = algA;
		this.algH = algH;
		this.seguro = seguridad;
	}

	@Override
	public void execute() {
		final Cliente client = new Cliente(seguro, puerto, cedula, algS, algA, algH);
	}

	@Override
	public void fail() {
		System.out.println("FAIL_TEST");
	}

	@Override
	public void success() {
		System.out.println("OK_TEST");
	}
}
