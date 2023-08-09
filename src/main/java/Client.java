import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

public class Client extends Thread{

	
	Socket socketClient;
	
	ObjectOutputStream out;
	ObjectInputStream in;
	
	private final Consumer<Serializable> callback;

	// Creates a client connection with the server
	// Uses ip and port numbers entered by player
	Client(Consumer<Serializable> call, String ipAddress, int port){
		if (port != 5555) {
			throw new IllegalArgumentException("Port number must be 5555");
		}
		callback = call;
		try {
			socketClient = new Socket(ipAddress, port);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socketClient.getInputStream());
			socketClient.setTcpNoDelay(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Reads in pokerInfo object sent from server
	public void run() {
		while(true) {
			 // Retrieves object from server
			try {
				pokerInfo object = (pokerInfo) in.readObject();
				callback.accept(object);
			} catch (IOException e) {
				callback.accept("Connection lost to the Server");
				try {
					in.close();
					out.close();
					socketClient.close();
					break;
				} catch (IOException ex) {
					callback.accept("No connection to server");
				}
			}
			catch(Exception e) {
				callback.accept("No connection to server");
				break;
			}
		}
	
    }

	// Sends pokerInfo object to the server to update
	public void send(pokerInfo info) {
		try {
			out.writeObject(info);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ClassCastException("Server Not Open");
		}
	}

}
