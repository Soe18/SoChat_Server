package Server;
import java.io.*;

import java.net.*;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class ServerSkeleton {
	public Semaphore hashMutex;
	public Server server;

	public ServerSkeleton() {
		hashMutex = new Semaphore(1);
		server = new Server();
	}

	public void exec(int port) {
		try{
			try (
				// creo la socket per il server
				ServerSocket serverComm = new ServerSocket(port)){
				System.out.println ("In attesa su porta 8080." );

				// creazione di un semaforo che mi permetterà di gestire hashmap

				while(true)
				{
					//metto il server in ascolto per accettare le richieste di connessione da parte dei client
					Socket clientsocket = serverComm.accept();
					System.out.println ("Aperta nuova comunicazione.");

					//creo il thread del server passando la socket e il valore del semaforo
					ServerSkeletonThread sThread = new ServerSkeletonThread(clientsocket, hashMutex, server);
					sThread.start();
				}
			}
		}catch ( IOException e ){
			e.printStackTrace();
		}
	}



}