package ua.adeptius.amocrm;


import ua.adeptius.amocrm.model.json.AmoAccount;
import ua.adeptius.amocrm.model.json.contact.Contact;
import ua.adeptius.amocrm.monitor.CookieCleaner;

public class TestClass {

    public static void main(String[] args) throws Exception {
        new CookieCleaner();

        String domain = "adeptius";
        String userLogin = "adeptius@wid.ua";
        String userApiKey = "a99ead2f473e150091360d25aecc2878";

//        String domain = "wid";
//        String userLogin = "adeptius@wid.ua";
//        String userApiKey = "a99ead2f473e150091360d25aecc2878";

//        AmoAccount amoAccount = AmoDAO.getAmoAccount(domain, userLogin, userApiKey);
//        String phoneId = amoAccount.getPhoneId();
//        System.out.println("phoneId="+phoneId);
//        String phoneEnumId = amoAccount.getPhoneEnumId();
//        System.out.println("phoneEnumId="+phoneEnumId);
//        int dealId = AmoDAO.addNewDeal(domain, userLogin, userApiKey);
//        AmoDAO.addNewContact(domain, userLogin, userApiKey, "0934027182", dealId, phoneId, phoneEnumId);

        String json = "{\n" +
                "        \"id\": 4790293,\n" +
                "        \"name\": \"Новый контакт\",\n" +
                "        \"last_modified\": 1499245426,\n" +
                "        \"account_id\": 15391081,\n" +
                "        \"date_create\": 1499202912,\n" +
                "        \"created_user_id\": 0,\n" +
                "        \"modified_user_id\": 0,\n" +
                "        \"responsible_user_id\": 1559047,\n" +
                "        \"group_id\": 0,\n" +
                "        \"closest_task\": 1499202912,\n" +
                "        \"linked_company_id\": \"0\",\n" +
                "        \"company_name\": \"\",\n" +
                "        \"tags\": [\n" +
                "          {\n" +
                "            \"id\": 47303,\n" +
                "            \"name\": \"СуперКлиент\",\n" +
                "            \"element_type\": 1\n" +
                "          }\n" +
                "        ],\n" +
                "        \"type\": \"contact\",\n" +
                "        \"custom_fields\": [\n" +
                "          {\n" +
                "            \"id\": \"153071\",\n" +
                "            \"name\": \"Телефон\",\n" +
                "            \"code\": \"PHONE\",\n" +
                "            \"values\": [\n" +
                "              {\n" +
                "                \"value\": \"0934027182\",\n" +
                "                \"enum\": \"335383\"\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ],\n" +
                "        \"linked_leads_id\": [\n" +
                "          \"1577351\"\n" +
                "        ]\n" +
                "      }";
        Contact contact = new Contact(json);

        System.out.println(contact);


//        String cookie = AmoDAO.auth(domain, userLogin, userApiKey);
//        System.out.println(cookie);

//        AmoAccount amoAccount = AmoDAO.getAmoAccount(domain, userLogin, userApiKey);
//        List<AmoAccount.Leads_status> leads_statuses = amoAccount.getLeads_statuses();
//        for (AmoAccount.Leads_status leads_status : leads_statuses) {
//            System.out.println(leads_status.getId() + " = " + leads_status.getName());
//        }

//        String contacts = AmoDAO.getContacts(domain, userLogin, userApiKey);
//        System.out.println(contacts);


//        AmoContact contact = AmoDAO.getContactIdByNumber(domain, userLogin, userApiKey, "5381237");
//        if (contact != null){ // контакт уже существует. Мы нашли его id по номеру с которого он звонил.
//            System.out.println("Контакт уже существует: "+contact);
//
//            // попробую его обновить - добавить сделку.
////            contact.getLinkedLeadsId().add("1496223");
////            contact.setLastModified((int)(new GregorianCalendar().getTimeInMillis() / 1000));
////            AmoDAO.updateContact(domain, userLogin, userApiKey, contact);
//
//
//        }else { // контакта нет - нужно его создать
//            // создаём контакт и ложим его id в поле id
//            System.out.println("Контакта с этим номером нет");
//            AmoContact newContact = new AmoContact();
//            newContact.setName("Java контакт");
//
//            ValuesItem valuesItem = new ValuesItem();
//            valuesItem.setEnumType("335393");
//            valuesItem.setValue("5381239");
//            ArrayList<ValuesItem> valuesItems = new ArrayList<>();
//            valuesItems.add(valuesItem);
//
//            CustomFieldsItem item = new CustomFieldsItem();
//            item.setId("153071");
//            item.setCode("PHONE");
//            item.setName("Телефон");
//            item.setValues(valuesItems);
//
//            List<CustomFieldsItem> customFieldsItems = new ArrayList<>();
//            customFieldsItems.add(item);
//            newContact.setCustomFields(customFieldsItems);
//
//            int id = AmoDAO.addNewContact(domain, userLogin, userApiKey, newContact);
//
//            System.out.println("айдишка нового контакта:" + id);
//
//        }
//
//
//
//
//        // нужно проверить существует ли уже сделка по этому контакту.
//        boolean dealAlreadyCreated = false;
//
//        if (dealAlreadyCreated){
//            // сделка уже существует
//            // возможно обновляем инфу, что пользователь позвонил еще раз.
//        }else {
//            // сделки нет. Нужно создать её и прикрепить к ней контакт с id.
//        }
//
//






    }


}
