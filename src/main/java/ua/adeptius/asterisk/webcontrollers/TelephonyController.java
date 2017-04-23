package ua.adeptius.asterisk.webcontrollers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/telephony")
public class TelephonyController {


    @RequestMapping(value = "/sipPasswords", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getTelephonyByName(@RequestParam String name, @RequestParam String password) {
//        if (!MainController.isTelephonyLogin(name, password)) {
//            return "Error: wrong password";
//        }
//
//        TelephonyCustomer customer;
//        try {
//           customer = MainController.getTelephonyByName(name);
//        } catch (NoSuchElementException e) {
//            return "Error: no such User";
//        }
//
//        try{
//            Map<String, String> map = PhonesDao.getSipPasswords(customer.getName());
//            return new JSONObject(map).toString();
//        }catch (Exception e){
//            return "Error: DB Error";
//        }
        return "";
    }
}
