package CostruzioneMSG;
import GetForms.MsgForm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;

public class CostruzioneContattiXML {

    private ArrayList<String> users;
    private static DocumentBuilderFactory docFactory;
    private static DocumentBuilder docBuilder;
    private static Boolean istanziato = false;

    public CostruzioneContattiXML(ArrayList<String> users)
    {
        //create a message (sender+receiver+text)
        this.users = users;
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
        e.setTextContent("contacts");
        rootElement.appendChild(e);
        for (int i = 0; i < users.size(); i++) {
            e = documento.createElement("user");
            e.setTextContent(users.get(i));
            rootElement.appendChild(e);
        }
        //return the document object
        return documento;
    }
}