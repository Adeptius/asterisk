package ua.adeptius.asterisk.model.telephony;


import java.util.Random;

public class SipConfig {

    public static void main(String[] args) {
            SipConfig sipConfig = new SipConfig("123");
    }

    private String password;
    private String number;

    public SipConfig(String number) {
        this.number = number;
        password = generatePassword();
    }
   public SipConfig(String number, String password) {
        this.number = number;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfig() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(number).append("]\n")
                .append("deny=0.0.0.0/0.0.0.0\n")
                .append("secret=").append(password).append("\n")
                .append("dtmfmode=rfc2833\n")
                .append("canreinvite=no\n")
                .append("context=from-internal\n")
                .append("host=dynamic\n")
                .append("defaultuser=\n")
                .append("trustrpid=yes\n")
                .append("sendrpid=pai\n")
                .append("type=friend\n")
                .append("session-timers=accept\n")
                .append("nat=no\n")
                .append("port=5060\n")
                .append("qualify=yes\n")
                .append("qualifyfreq=60\n")
                .append("transport=udp\n")
                .append("avpf=no\n")
                .append("maxlen=2\n")
                .append("force_avp=no\n")
                .append("icesupport=no\n")
                .append("encryption=no\n")
                .append("namedcallgroup=\n")
                .append("namedpickupgroup=\n")
                .append("dial=SIP/").append(number).append("\n")
                .append("permit=0.0.0.0/0.0.0.0\n")
                .append("callerid=").append(number).append(" <").append(number).append(">\n")
                .append("callcounter=yes");
        return sb.toString();
    }

    private String generatePassword() {
        String str = "0123456789abcdefghijklmnopqrstuvwxyz";
        String randomPass = "";
        Random random = new Random();
        for (int i = 0; i < 7; i++) {
            randomPass += str.charAt(random.nextInt(36));
        }
        return randomPass;
    }
}
