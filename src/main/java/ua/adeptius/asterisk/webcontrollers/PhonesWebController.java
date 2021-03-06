package ua.adeptius.asterisk.webcontrollers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.SipConfigDao;
import ua.adeptius.asterisk.json.JsonInnerAndOuterPhones;
import ua.adeptius.asterisk.json.JsonPhoneCount;
import ua.adeptius.asterisk.json.JsonPhoneGroup;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.telephony.InnerPhone;
import ua.adeptius.asterisk.model.telephony.OuterPhone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.telephony.PhoneGroup;
import ua.adeptius.asterisk.monitor.Scheduler;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.json.Message.Status.Error;
import static ua.adeptius.asterisk.json.Message.Status.Success;

@Controller
@ResponseBody
@RequestMapping(value = "/phones", produces = "application/json; charset=UTF-8")
public class PhonesWebController {

    private boolean safeMode = true;
    private static Logger LOGGER = LoggerFactory.getLogger(PhonesWebController.class.getSimpleName());


    @PostMapping("/getSiteOuter")
    public Object getBlackList(HttpServletRequest request, @RequestParam String siteName) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        Set<Site> sites = user.getSites();
        if (sites == null || sites.isEmpty()) {
            return new Message(Error, "User have no such site");
        }
        Site site = user.getSiteByName(siteName);
        if (site == null) {
            return new Message(Error, "User have no such site");
        }

        return site.getOuterPhones();
    }


    @PostMapping("/getAllPhones")
    public Object getOuterAndInner(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }
        return new JsonInnerAndOuterPhones(user.getInnerPhones(), user.getOuterPhones());
    }

    @PostMapping("/setNumberCount")
    public Message setNumberCount(@RequestBody JsonPhoneCount jsonPhoneCount, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        String login = user.getLogin();

        Set<OuterPhone> outerPhones = user.getOuterPhones();
        int currentOuterNumberCount = outerPhones.size();
        int neededOuterNumberCount = jsonPhoneCount.getOuterCount();

        Set<InnerPhone> innerPhones = user.getInnerPhones();
        int currentInnerNumberCount = innerPhones.size();
        int neededInnerNumberCount = jsonPhoneCount.getInnerCount();

        if (neededInnerNumberCount < 0) {
            return new Message(Error, "Inner count cant be lower than 0");
        } else if (neededInnerNumberCount > 50) {
            return new Message(Error, "Inner count cant be higher than 50. Contact us if you need more");
        }

        if (neededOuterNumberCount < 0) {
            return new Message(Error, "Outer count cant be lower than 0");
        } else if (neededOuterNumberCount > 50) {
            return new Message(Error, "Outer count cant be higher than 50. Contact us if you need more");
        }


        LOGGER.info("{}: Запрос изменения количества номеров. Внешние {} -> {}, внутренние {} -> {}",
                login, currentOuterNumberCount, neededOuterNumberCount, currentInnerNumberCount, neededInnerNumberCount);

        try {
            if (neededOuterNumberCount > currentOuterNumberCount) {
                int needMoreCount = neededOuterNumberCount - currentOuterNumberCount;
                LOGGER.debug("{}: Нужно дополнительно {} внешних номеров.", login, needMoreCount);

                List<OuterPhone> freeOuterPhones = HibernateController.getAllFreeOuterPhones(); // вот список свободных
                int weHaveNumbers = freeOuterPhones.size();//вот сколько их есть
                LOGGER.debug("{}: В наличии есть {} внешних номеров.", login, weHaveNumbers);

                if (needMoreCount > weHaveNumbers) { // если номеров недостаточно
                    LOGGER.debug("{}: В наличии есть {} внешних номеров. Нужно {}. Возвращаем ошибку", login, weHaveNumbers, needMoreCount);
                    return new Message(Error, "Not enough numbers at the moment");
                }
                // чтож, их достаточно. берём новое количество
                List<OuterPhone> newPhone = freeOuterPhones.stream().limit(needMoreCount).collect(Collectors.toList());
                user.addOuterPhones(newPhone);
                LOGGER.debug("{}: Добавлено {} внешних номеров. Теперь их {}", login, newPhone.size(), outerPhones.size());


            } else if (neededOuterNumberCount < currentOuterNumberCount) {
                int redutrantCount = currentOuterNumberCount - neededOuterNumberCount;
                LOGGER.debug("{}: Внешних номеров больше чем нужно. Лишних {}", login, redutrantCount);
                List<OuterPhone> redutrantNumbers = outerPhones.stream() // Выбираем последние лишние телефоны
                        .sorted((o1, o2) -> o2.getNumber().compareTo(o1.getNumber()))
                        .limit(redutrantCount)
                        .collect(Collectors.toList());
                redutrantNumbers.forEach(phone -> phone.setSitename(null)); // освобождаем номер на случай, если он был привязан к сайту
                HibernateController.markOuterPhoneFree(redutrantNumbers.stream().map(OuterPhone::getNumber).collect(Collectors.toList()));
                user.removeOuterPhones(redutrantNumbers);// и наконец удаляем их у пользователя
//                outerPhones.removeAll(redutrantNumbers);
                LOGGER.debug("{}: Удалено {} внешних номеров. Теперь их {}", login, redutrantNumbers.size(), outerPhones.size());

                // изменено количество внешних номеров. Обновляем Кэши

            } else {
                LOGGER.debug("{}: Количество внешних номеров не меняется", login);
            }


            // теперь внутренние номера
            if (neededInnerNumberCount > currentInnerNumberCount) {
                int needMoreCount = neededInnerNumberCount - currentInnerNumberCount;
                LOGGER.debug("{}: Нужно дополнительно {} внутренних номеров.", login, needMoreCount);

                List<InnerPhone> moreSipNumbers = SipConfigDao.createMoreSipNumbers(needMoreCount, login);
                user.addInnerPhones(moreSipNumbers);
//                innerPhones.addAll(moreSipNumbers);
                LOGGER.debug("{}: Добавлено {} внутренних номеров. Теперь их {}", login, moreSipNumbers.size(), innerPhones.size());

            } else if (neededInnerNumberCount < currentInnerNumberCount) {
                int redutrantCount = currentInnerNumberCount - neededInnerNumberCount;
                LOGGER.debug("{}: Внутренних номеров больше чем нужно. Лишних {}", login, redutrantCount);

                List<InnerPhone> redutrantNumbers = innerPhones.stream()
                        .sorted((o1, o2) -> o2.getNumber().compareTo(o1.getNumber()))
                        .limit(redutrantCount)
                        .collect(Collectors.toList());
//                innerPhones.removeAll(redutrantNumbers);
                user.removeInnerPhones(redutrantNumbers);
                SipConfigDao.removeSipNumbersConfigs(redutrantNumbers);
                LOGGER.debug("{}: Удалено {} внутренних номеров. Теперь их {}", login, redutrantNumbers.size(), innerPhones.size());

            } else {
                LOGGER.debug("{}: Количество внутренних номеров не меняется", login);
            }

            HibernateController.update(user);
            return new Message(Success, "Number count set");
        } catch (Exception e) {
            LOGGER.error(login + ": ошибка изменения количества номеров телефонии: " + jsonPhoneCount, e);
            return new Message(Error, "Internal error");
        } finally {
            if (safeMode)
                try {
//                    CallProcessor.updatePhonesHashMap();
                    user = HibernateController.getUserByLogin(user.getLogin());
                } catch (Exception e) {
                    LOGGER.error(user.getLogin() + ": ошибка синхронизации после изменения количества номеров", e);
                }
        }
    }

    @PostMapping("/setGroup")
    public Object getBlackList(HttpServletRequest request, @RequestBody JsonPhoneGroup jsonPhoneGroup) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        String name = jsonPhoneGroup.getName();
        if (StringUtils.isBlank(name)){
            return new Message(Error, "Name is empty");
        }

        List<String> phones = jsonPhoneGroup.getPhones();
        if (phones == null || phones.size() == 0) {
            return new Message(Error, "Phones is empty");
        }

        phones = phones.stream().distinct().collect(Collectors.toList());

        List<String> listOfCurrentNumbers = user.getInnerPhones().stream().map(InnerPhone::getNumber).collect(Collectors.toList());

        // Проверяем сначала что все присланные номера есть у пользователя.
        for (String phone : phones) {
            if (!listOfCurrentNumbers.contains(phone)){
                return new Message(Error, "Wrong number " + phone);
            }
        }

        // Все номера правильные. Теперь проверяем не указаны ли эти номера в других группах
        Set<PhoneGroup> phoneGroups = user.getPhoneGroups();
        for (PhoneGroup phoneGroup : phoneGroups) {
            if (phoneGroup.getName().equals(name)){
                continue; // это та же группа. Интересуют только другие Пропускаем.
            }
            List<String> numbersFromOtherGroup = phoneGroup.getPhones();
            for (String numberFromOtherGroup : numbersFromOtherGroup) {
                if (phones.contains(numberFromOtherGroup)){
                    return new Message(Error, "Number already in group: " + numberFromOtherGroup);
                }
            }
        }


        PhoneGroup phoneGroup = user.getPhoneGroups().stream()
                .filter(pg -> pg.getName().equals(name))
                .findFirst().orElse(null);

        if (phoneGroup == null){
            phoneGroup = new PhoneGroup();
            phoneGroup.setName(name);
        }

//        PhoneGroup phoneGroup = new PhoneGroup();
        phoneGroup.setPhones(phones);
        user.addPhoneGroup(phoneGroup);
        try {
            HibernateController.update(user);
            return new Message(Success, "Setted");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка установки группы " + jsonPhoneGroup, e);
            return new Message(Error, "Internal error");
        }
    }

    @PostMapping("/getGroup")
    public Object getGroup(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        return user.getPhoneGroups();
    }


    @PostMapping("/removeGroup")
    public Object removeGroup(HttpServletRequest request, String name) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        if (StringUtils.isBlank(name)){
            return new Message(Error, "Name is empty");
        }

        PhoneGroup phoneGroupByName = user.getPhoneGroupByName(name);
        if (phoneGroupByName == null) {
            return new Message(Error, "Group not found");
        }

        user.removePhoneGroup(phoneGroupByName);
        try {
            HibernateController.update(user);
            return new Message(Success, "Removed");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка удаления группы " + name, e);
            return new Message(Error, "Internal error");
        }
    }
}
