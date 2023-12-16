package Server;

import CostruzioneMSG.CostruzioneChatXML;
import CostruzioneMSG.CostruzioneContattiXML;
import GetForms.LogForm;
import GetForms.MsgForm;
import GetForms.RegForm;
import org.w3c.dom.Document;

import java.util.ArrayList;

public class Server {

    DBManager dbManager;
    public Server() {
        dbManager = new DBManager();
    }

    /**
     *
     * @return 0 if successful
     * @return 1 if user doesn't exists
     * @return 2 if passwords don't match in db
     */
    public int login(LogForm logForm) {
        int reply = -1;
        String password = dbManager.checkUser(logForm.getNickname());

        if (password == null) {
            reply = 1;
        }
        else if (password.equals(logForm.getPassword())) {
            reply = 0;
        }
        else {
            reply = 2;
        }

        return reply;
    }

    /**
     *
     * @return 0 if successful
     * @return 3 if passwords don't match between them
     * @return 4 if user already exists
     */
    public int register(RegForm regForm, boolean deep) {
        int reply = 3;
        String password = dbManager.checkUser(regForm.getNickname());

        System.out.println(password);

        if (password != null) {
            reply = 4;
            return reply;
        }

        System.out.println(regForm.getPassword()+"-"+regForm.getConfirmPassword());

        if (regForm.getPassword().equals(regForm.getConfirmPassword())) {
            reply = 0;
            // Aggiungo utente su DB
            dbManager.updateDBWithNewUser(regForm.getNickname(), regForm.getPassword(), deep);
        }

        System.out.println(reply);
        return reply;
    }

    // sendMSG
    public void sendMSG(MsgForm msgForm, boolean deep) {
        dbManager.saveMSG(msgForm.getSender(), msgForm.getReceiver(), msgForm.getText(), deep);
    }

    public Document loadChat(String user) {
        ArrayList<MsgForm> messages = dbManager.getChats(user);
        CostruzioneChatXML costruzioneChatXML = new CostruzioneChatXML(messages);
        return costruzioneChatXML.getXMLObject();
    }

    public Document loadContacts() {
        ArrayList<String> users = dbManager.getUsers();
        CostruzioneContattiXML costruzioneContattiXML = new CostruzioneContattiXML(users);
        return costruzioneContattiXML.getXMLObject();
    }
}
