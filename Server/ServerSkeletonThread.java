package Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

import CostruzioneMSG.CostruzioneMessaggioXML;
import GetForms.LogForm;
import GetForms.MsgForm;
import GetForms.RegForm;
import GetForms.XMLInterpreter;
import org.w3c.dom.*;

public class ServerSkeletonThread extends Thread {
	private Server server;
	private Socket client;
	private String clientName;
	private Semaphore hmMutex;
	private ObjectInputStream inBuffer;
	private ObjectOutputStream outBuffer;
	private XMLInterpreter xmlInterpreter;
	private Boolean ended;

	public ServerSkeletonThread(Socket socket, Semaphore hashMutex, Server server){
		this.client = socket;
		this.hmMutex = hashMutex;
		this.server = server;
		xmlInterpreter = new XMLInterpreter();
		ended = false;
	}
	
	public void run() {
		try{
			outBuffer = new ObjectOutputStream(client.getOutputStream());
			inBuffer = new ObjectInputStream(client.getInputStream());

			// Registrazione utente
			Document doc = (Document)inBuffer.readObject();

			int type = xmlInterpreter.getTypeOfComunication(doc);

			int codeReply = -1;

			// Ora devo accertare che utente sia inserito nel db
			// se non lo e', ended diventa false,
			if (type == 1) {
				LogForm logForm = xmlInterpreter.returnLoginInfos(doc);
				codeReply = server.login(logForm);
				clientName = logForm.getNickname();
			}
			// Ora devo accertare che utente non sia gia' nel db
			// e che password siano corrette
			else if (type == 0) {
				RegForm regForm = xmlInterpreter.returnRegisterInfos(doc);
				codeReply = server.register(regForm, isAlive());
				clientName = regForm.getNickname();
			}
			else {
				ended = true;
			}

			// client si dovra' aspettare una risposta, tale sara' la codeReply
			if (codeReply > 0) {
				outBuffer.writeUTF("errcode: " + Integer.toString(codeReply));
				ended = true;
			}
			else {
				System.out.println(clientName);
				hmMutex.acquire();
				ClientSockets.getInstance().hashMap.put(clientName, outBuffer);
				hmMutex.release();

			}

			// Main loop
			while (!ended)
			{
				// waiting for client xml message
				System.out.println ("	In attesa messaggio dal client..." );
				Document msgDoc = (Document)inBuffer.readObject();
				// get the message content
				type = xmlInterpreter.getTypeOfComunication(msgDoc);

				if (type == 2) { // send msg
					MsgForm msgForm = xmlInterpreter.returnMessageInfos(msgDoc);
					System.out.println ("Messaggio ricevuto <" + msgForm.getText() + ">. Invio risposta, attendere..." );
					server.sendMSG(msgForm, isAlive());

					hmMutex.acquire();
					ObjectOutputStream toEndPoint = ClientSockets.getInstance().hashMap.get(msgForm.getReceiver());
					hmMutex.release();

					CostruzioneMessaggioXML msgXML = new CostruzioneMessaggioXML(msgForm.getText(), msgForm.getSender(), msgForm.getReceiver());
					Document reply = msgXML.getXMLObject();
					toEndPoint.writeObject(reply);
					outBuffer.writeUTF("Messaggio inviato!");
				}
				else if (type == 5) {
					// load contact
					String nickname = xmlInterpreter.returnLoadContactsInfos(msgDoc);
					Document loadContacts = server.loadContacts(nickname);
					outBuffer.writeObject(loadContacts);
				}
				else if (type == 3) {
					ended = true;
				}
			}

			// Salvataggio nel DB, entro in sezione critica
			//dbManager.writeOnDisc();

			//chiudo i buffer e la socket
			inBuffer.close();
			outBuffer.close();
			client.close();

		}catch (Exception e ){
			e.printStackTrace();
		}
	}

}