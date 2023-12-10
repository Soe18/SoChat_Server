package Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

import org.w3c.dom.*;

public class ServerThread extends Thread {
	
	private Socket client;
	private String clientName;
	private Semaphore Sem;
	private ObjectInputStream inBuffer;
	private DataOutputStream outBuffer; 
	private DocumentBuilder builder;
	private File xmlFile;
	private Document db;
	protected static final String XML_FILE_NAME = "DatabaseChat/DBchat.xml";

	public ServerThread (Socket skt, Semaphore SemBin){
		client=skt;
		Sem=SemBin;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            
            // Creo il parser DOM
            builder = factory.newDocumentBuilder();

            //creo il file
            xmlFile = new File(XML_FILE_NAME);
            
			//gestione della sezione critica (al cui interno deve essere presente 1 thread alla volta)
			Sem.acquire();
            db = builder.parse(xmlFile);
			Sem.release();

			//indentazione rimuovendo i whitespace
			removeWhitespaces(db.getDocumentElement()); // TODO guarda bene this         <<<------
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run() {
		try{
			outBuffer = new DataOutputStream(client.getOutputStream());
			inBuffer = new ObjectInputStream(client.getInputStream());

			// Registrazione utente
			Document regDoc = (Document)inBuffer.readObject();
			clientName = regDoc.getElementsByTagName("nickname").item(0).getTextContent();

			updateDBWithNewUser(regDoc);
			outBuffer.writeUTF("Registrazione eseguita.");

			// Rendiamo nota la connessione


			Boolean ended = false;
			while (!ended)
			{
				// waiting for client xml message
				System.out.println ("	In attesa messaggio dal client..." );
				Document msgDoc = (Document)inBuffer.readObject();
				// get the message content
				String Message = msgDoc.getElementsByTagName("Content").item(0).getTextContent();
				System.out.println ("Messaggio ricevuto < " + Message + ">. Invio risposta, attendere..." );

				if (Message.equals("fine")) {
					ended = true;
				}
				else {
					// Manda messaggio a destinatario


					// aggiorno il database con il messaggio ricevuto
					UpdateXMLDB(msgDoc);
				}
				// send response
				outBuffer.writeUTF(Message);
			}

			// Salvataggio nel DB, entro in sezione critica
			writeOnDisc();

			//chiudo i buffer e la socket
			inBuffer.close();
			outBuffer.close();
			client.close();

		}catch (Exception e ){
			e.printStackTrace();
		}
	}

	/**
	 * Metodo necessario per salvare nel "db"
	 * il messaggio inviato
	 * @param m, messaggio inviato
	 */
	public void UpdateXMLDB(Document m)
	{
		      
        try {
            //get sender and receiver by xml message object
			String sender = m.getElementsByTagName("Sender").item(0).getTextContent();
			String receiver = m.getElementsByTagName("Receiver").item(0).getTextContent();
			//search the chat element with id sender_receiver
			Element chat = db.getElementById(sender+"_"+receiver);
			//import chat message node from xml message object to xml db object and get it in newMessage
			Node newMessage = db.importNode(m.getDocumentElement() /*m.getElementsByTagName("ChatMessage").item(0)*/, isAlive());
			
			if(chat != null)
			{
				chat.appendChild(newMessage);
			}
			else
			{
				// caso in cui la comunicazione tra sender=paolo e receiver=andrea sia iniziata
				// prima da andrea a paolo, perche' id chat sara' sender=andrea e receiver=paolo
				chat = db.getElementById(receiver+"_"+sender);
				if(chat != null)
				{
					chat.appendChild(newMessage);
				}
				else
				{
					// se non esiste nemmeno con la combinazione inversa, si tratta di una nuova chat
					Element chatNew = db.createElement("chat");
					chatNew.setAttribute("id", sender+"_"+receiver);
					chatNew.appendChild(newMessage);
					Element chatList = (Element)db.getElementsByTagName("chatlist").item(0);
					chatList.appendChild(chatNew);
				}
			}   

        } catch (Throwable t) {
            t.printStackTrace();
        }
        
    }

	//questo presumo non sia nulla di che, dovrebbe riguardare i whitespace
	public static void removeWhitespaces(Element element) {
		NodeList children = element.getChildNodes();
		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node child = children.item(i);
			if (child instanceof Text
				&& ((Text) child).getData().trim().isEmpty()) {
				element.removeChild(child);
			} else if (child instanceof Element) {
				removeWhitespaces((Element) child);
			}
		}
	}

	private void updateDBWithNewUser(Document r) {
		try {
			//get sender and receiver by xml message object
			String nickname = r.getElementsByTagName("nickname").item(0).getTextContent();
			String password = r.getElementsByTagName("password").item(0).getTextContent();
			//search the chat element with id sender_receiver
			Node userlist = db.getElementsByTagName("userlist").item(0);
			//import chat message node from xml message object to xml db object and get it in newMessage
			Node newUser = db.importNode(r.getDocumentElement() /*m.getElementsByTagName("ChatMessage").item(0)*/, isAlive());

			userlist.appendChild(newUser);
			// entro in sezione critica
			writeOnDisc();

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private void writeOnDisc() {
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(db);
			StreamResult result = new StreamResult(xmlFile);

			//It is needed to indent the xml file
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DocumentType doctype = db.getDoctype();
			//it is needed to include the dtd declaration in xml file
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());

			// Sezione critica, salvo il messaggio nel "db"
			Sem.acquire();
			transformer.transform(source, result);
			Sem.release();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}

}