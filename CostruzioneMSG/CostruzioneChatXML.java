package CostruzioneMSG;
import GetForms.MsgForm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;

public class CostruzioneChatXML {

    private ArrayList<MsgForm> messages;
    private static DocumentBuilderFactory docFactory;
    private static DocumentBuilder docBuilder;
    private static Boolean istanziato = false;

    public CostruzioneChatXML(ArrayList<MsgForm> messages)
    {
        //create a message (sender+receiver+text)
        this.messages = messages;
        if(!istanziato)
        {
            try
            {
                docFactory = DocumentBuilderFactory.newInstance();
                docBuilder = docFactory.newDocumentBuilder();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            istanziato = true;
        }
    }

    public Document getXMLObject()
    {
        //Create an xml object (Document)
        Document documento = docBuilder.newDocument();
        //Create the root element
        Element rootElement = documento.createElement("SoChat");
        //Add the element to the xml document
        documento.appendChild(rootElement);

        //Add Content, Sender and Receiver to the ChatMessage element as children elements
        Element e = documento.createElement("Type");
        e.setTextContent("loadchats");
        rootElement.appendChild(e);
        Element contentElement = documento.createElement("Chat");
        for (int i = 0; i < messages.size(); i++) {
            e = documento.createElement("Content");
            e.setTextContent(messages.get(i).getText());
            contentElement.appendChild(e);
            e = documento.createElement("Sender");
            e.setTextContent(messages.get(i).getSender());
            contentElement.appendChild(e);
            e = documento.createElement("Receiver");
            e.setTextContent(messages.get(i).getReceiver());
            contentElement.appendChild(e);
        }
        rootElement.appendChild(contentElement);
        //return the document object
        return documento;
    }
}