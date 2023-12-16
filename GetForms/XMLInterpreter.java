package GetForms;

import org.w3c.dom.Document;

public class XMLInterpreter {

    public XMLInterpreter() {
    }

    /**
     *
     * @param xml Document xml, well formatted by client
     * @return 0 if type equals "register"
     * @return 1 if type equals "login"
     * @return 2 if type equals "message"
     * @return 3 if type equals "end"
     * @return -1 if an error as occurred
     */
    public int getTypeOfComunication(Document xml) {
        System.out.println(xml.getFirstChild());


        String type = xml.getElementsByTagName("Type").item(0).getTextContent();
        int result;

        switch (type) {
            case "register":
                result = 0;
                break;
            case "login":
                result = 1;
                break;
            case "message_forward":
                result = 2;
                break;
            case "end":
                result = 3;
                break;
            case "message_enter":
                result = 4;
                break;
            default:
                result = -1;
                break;
        }
        return result;
    }

    public MsgForm returnMessageInfos(Document xml) {
        String sender = xml.getElementsByTagName("Sender").item(0).getTextContent();
        String receiver = xml.getElementsByTagName("Receiver").item(0).getTextContent();
        String text = xml.getElementsByTagName("Content").item(0).getTextContent();
        return new MsgForm(sender, receiver, text);
    }

    public RegForm returnRegisterInfos(Document xml) {
        String nickname = xml.getElementsByTagName("Nickname").item(0).getTextContent();
        String password = xml.getElementsByTagName("Password").item(0).getTextContent();
        String confirmPassword = xml.getElementsByTagName("ConfirmPassword").item(0).getTextContent();
        return new RegForm(nickname, password, confirmPassword);
    }

    public LogForm returnLoginInfos(Document xml) {
        String nickname = xml.getElementsByTagName("Nickname").item(0).getTextContent();
        String password = xml.getElementsByTagName("Password").item(0).getTextContent();
        return new LogForm(nickname, password);
    }

}
