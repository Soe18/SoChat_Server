package Server;

import GetForms.LogForm;
import GetForms.MsgForm;
import org.w3c.dom.*;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class DBManager {
    private static final String XML_FILE_NAME = "DatabaseChat/DBchat.xml";
    private DocumentBuilder builder;
    private File xmlFile;
    public Document db;

    public DBManager() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try{
            // Creo il parser DOM
            builder = factory.newDocumentBuilder();
            // apro il file xml
            xmlFile = new File(XML_FILE_NAME);
            // salvo in memoria db
            db = builder.parse(xmlFile);
            // rimuoviamo whitespaces
            removeWhitespaces(db.getDocumentElement());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void removeWhitespaces(Element element) {
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

    /**
     * Metodo necessario per salvare nel "db"
     * il messaggio inviato
     */
    public synchronized void saveMSG(String sender, String receiver, String text, boolean deep) {
        try {
            Document m = buildChat(sender, receiver, text);
            //import chat message node from xml message object to xml db object and get it in newMessage
            Node newMessage = db.importNode(m.getDocumentElement(), deep);
            Node chat = findChat(sender, receiver);
            //Node chat = db.getElementById(sender+"_"+receiver);

            System.out.println("1: "+chat);
            if(chat != null) {
                chat.appendChild(newMessage);
            }
            else {
                // se non esiste nemmeno con la combinazione inversa, si tratta di una nuova chat
                Element chatNew = db.createElement("Chat");
                chatNew.setAttribute("id", sender+"_"+receiver);
                chatNew.appendChild(newMessage);
                Element chatList = (Element)db.getElementsByTagName("ChatList").item(0);
                chatList.appendChild(chatNew);
                System.out.println("done");
            }
            writeOnDisc();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public synchronized Document buildChat(String sender, String receiver, String text) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = doc.createElement("ChatMessage");
            //Add the element to the xml document
            doc.appendChild(rootElement);

            //Add Content, Sender and Receiver to the ChatMessage element as children elements
            Element e = doc.createElement("Content");
            e.setTextContent(text);
            rootElement.appendChild(e);
            e = doc.createElement("Sender");
            e.setTextContent(sender);
            rootElement.appendChild(e);
            e = doc.createElement("Receiver");
            e.setTextContent(receiver);
            rootElement.appendChild(e);
            return doc;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized Document buildUser(String nickname, String password) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = doc.createElement("User");
            rootElement.setAttribute("id", nickname);
            //Add the element to the xml document
            doc.appendChild(rootElement);

            Element e = doc.createElement("Password");
            e.setTextContent(password);
            rootElement.appendChild(e);
            return doc;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param nickname
     * @return null if user doesn't exists
     * @return String password if user exists and its password
     */
    public synchronized String checkUser(String nickname) {
        String result = null;
        Node currNode = db.getElementById(nickname);

        if (currNode != null) {
            result = currNode.getFirstChild().getTextContent();
            System.out.println("Password: "+result);
        }

        return result;
    }

    // to rewrite
    public synchronized void updateDBWithNewUser(String nickname, String password, boolean deep) {
        try {
            Document r = buildUser(nickname, password);
            Node userlist = db.getElementsByTagName("UserList").item(0);
            Node newUser = db.importNode(r.getDocumentElement(), deep);

            userlist.appendChild(newUser);
            // entro in sezione critica
            writeOnDisc();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public synchronized void writeOnDisc() {
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
            transformer.transform(source, result);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public synchronized Node findChat(String s, String r) {
        NodeList allChats = db.getElementsByTagName("Chat");
        Node result = null;

        for (int i = 0; i < allChats.getLength(); i++) {
            String chatName = allChats.item(i).getAttributes().getNamedItem("id").getTextContent();
            System.out.println(chatName);
            if (chatName.equals(s+"_"+r) || chatName.equals(r+"_"+s)) {
                result = allChats.item(i);
                break;
            }
        }
        return result;
    }

    public synchronized ArrayList<String> getUsers(String nickname) {
        NodeList allUsers = db.getElementsByTagName("User");
        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < allUsers.getLength(); i++) {
            String currUser = allUsers.item(i).getAttributes().getNamedItem("id").getTextContent();
            if (!currUser.equals(nickname)) {
                result.add(currUser);
            }
        }
        return result;
    }

    public synchronized ArrayList<MsgForm> getChat(String user, String requestedUser) {
        ArrayList<MsgForm> messages = new ArrayList<>();
        NodeList allChats = db.getElementsByTagName("Chat");
        String tmp = "";

        for (int i = 0; i < allChats.getLength(); i++) {
            tmp = allChats.item(i).getAttributes().getNamedItem("id").getTextContent();
            if (tmp.contains(user) && tmp.contains(requestedUser)) {
                NodeList chat = allChats.item(i).getChildNodes();
                for (int j = 0; j < chat.getLength(); j++) {
                    String t = chat.item(j).getChildNodes().item(0).getTextContent();
                    String s = chat.item(j).getChildNodes().item(1).getTextContent();
                    String r = chat.item(j).getChildNodes().item(2).getTextContent();
                    messages.add(new MsgForm(s, r, t));
                }
            }
        }
        return messages;
    }


    public static void main(String[] args) {
        DBManager db = new DBManager();
        //db.findChat("nick","fabiano");
        System.out.println(db.getChat("nick", "fabiano"));
    }
}
