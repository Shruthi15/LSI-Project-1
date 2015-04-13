package rpctest;

public class TestRPCServer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Thread t = new Thread(new RPCServer());
		t.start();
	}

}
