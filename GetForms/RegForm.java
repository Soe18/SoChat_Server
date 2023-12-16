package GetForms;

public class RegForm {
    private String nickname;
    private String password;
    private String confirmPassword;

    public RegForm(String nickname, String password, String confirmPassword) {
        this.nickname = nickname;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
