package Server;
import java.io.*;

import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Server {

	public static void main(String[] args) {
		try{
			try (
				// creo la socket per il server
				ServerSocket server = new ServerSocket ( 8080 )) {
				System.out.println ("In attesa su porta 8080." );
				// creazione di un semaforo che mi permetterà di gestire le connessioni in entrata
				Semaphore SemBin = new Semaphore(1);
				while(true)
				{
					//metto il server in ascolto per accettare le richieste di connessione da parte dei client
					Socket clientsock = server.accept();
					System.out.println ("Nuovo client connesso." );

					//creo il thread del server passando la socket e il valore del semaforo
					ServerThread sThread = new ServerThread(clientsock,SemBin);
					sThread.start();
				}
			}
		}catch ( IOException e ){
			e.printStackTrace();
		}
	}
}