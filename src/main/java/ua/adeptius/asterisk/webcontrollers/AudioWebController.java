package ua.adeptius.asterisk.webcontrollers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.Settings;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Rule;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.UserAudio;
import ua.adeptius.asterisk.utils.AudioConverter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.json.Message.Status.Error;
import static ua.adeptius.asterisk.json.Message.Status.Success;


@Controller
@RequestMapping(value = "/audio", produces = "application/json; charset=UTF-8")
@ResponseBody
public class AudioWebController {

    private static Logger LOGGER = LoggerFactory.getLogger(AudioWebController.class.getSimpleName());
    private static Settings settings = Main.settings;

    @PostMapping(value = "/getList")
    public Object getScript(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }
        return user.getUserAudio();
    }


    @ResponseBody
    @PostMapping("/add")
    public Message singleFileUpload(HttpServletRequest request,
                                   @RequestParam("file") MultipartFile file, @RequestParam("name") String name) {

        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }
        String login = user.getLogin();


        if (file.isEmpty()) {
            return new Message(Error, "File is null");
        }

        String incomingFileName = file.getOriginalFilename();
        if (!incomingFileName.contains(".")) {
            return new Message(Error, "Wrong format"); // Отсутствет расширение
        }

        if (name.length() > 200) {
            return new Message(Error, "Name length more than 200 chars");
        }

        Optional<UserAudio> first = user.getUserAudio().stream()
                .filter(userMelody -> userMelody.getName().equals(name))
                .findFirst();
        if (first.isPresent()) {
            return new Message(Error, "Same name already present");
        }

        File tempFile = null;
        try { // файл пришел
            LOGGER.info("{}: запрос добавления мелодии {}, названный как '{}' ", login, incomingFileName, name);
            String USER_MUSIC_FOLDER = settings.getFolderUserMusic();

            byte[] bytes = file.getBytes(); // эдесь файл временно в оперативке

            String incomingFileExtention = incomingFileName.substring(incomingFileName.lastIndexOf(".") + 1);

            // создаём временный файл что бы записать из оперативки в файл для ffmpeg
            tempFile = File.createTempFile("tempfile", "." + incomingFileExtention);

            // И записываем всё в этот файл
            String tempPath = tempFile.getAbsolutePath();
            Path path = Paths.get(tempPath);
            Files.write(path, bytes);
            LOGGER.debug("{}: файл сохранён временно в {}", login, path);

            // Выясняем в какую папку будем ложить новый файл
            String separator = File.separator;
            Path newPathFolder = Paths.get(USER_MUSIC_FOLDER + login + separator);
            if (!Files.exists(newPathFolder)) { // создаём её, если отсутствует. ffmpeg этого делать не умеет
                Files.createDirectory(newPathFolder);
            }

            // Указываем новое имя файла с транслитерацией
            String newFileName = transliterate(name) + ".mp3";
            String newFilePath = newPathFolder.toAbsolutePath() + separator + newFileName;
            LOGGER.debug("{}: переконвертированный файл будет сохранён как {}", login, newFilePath);


            // Возможен вариант, что из-за транслитерации файл уже такой может существовать, так как спец символы пропадают
            Path path1 = Paths.get(newFilePath);
            if (Files.exists(path1)) {
                Files.delete(path1);
                LOGGER.debug("{}: файл {} уже существовал. Удалён.", login, newFilePath);
//                return new Message(Error, "Already exists");
            }
            //todo очистка папки темп шедулером

            // на данном этапе всё готово для кодирования


            LOGGER.debug("{}: конвертация {} -> {}", login, tempPath, newFilePath);
            int convertedSize = AudioConverter.convert(tempPath, newFilePath,30);
            if (!Main.startedOnWindows) {// если запускается локально на винде - chmod не требуется
                Runtime.getRuntime().exec("chmod 644 " + newFilePath);
            }
            LOGGER.debug("{}: конвертация завершена. Размер файла {} кб", login, convertedSize);

            if (convertedSize == -1){
                LOGGER.error("{}: конвертация завершена неудачно", login);
                return new Message(Error, "Internal error");
            }else if (convertedSize == 0){
                return new Message(Error, "Broken file");
            }

            UserAudio newMelody = new UserAudio();
            newMelody.setName(name);
            newMelody.setFilename(newFileName);
            newMelody.setLogin(login);
            user.addUserAudio(newMelody);
            HibernateController.update(user);
            LOGGER.debug("{}: новая пользовательская мелодия сохранена в БД: {}", login, newMelody);

            return new Message(Success, "Saved");
        } catch (Exception e) {
            LOGGER.error(login + ": ошибка при сохранении пользовательской мелодии", e);
        } finally { // просто чистка. Удаление временного файла.
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        return new Message(Error, "Internal error");
    }

    public static String transliterate(String message) {
        char[] abcCyr = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', ' ', 'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        String[] abcLat = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "_", "a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "i", "", "e", "ju", "ja", "A", "B", "V", "G", "D", "E", "E", "Zh", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ts", "Ch", "Sh", "Sch", "", "I", "", "E", "Ju", "Ja", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            for (int x = 0; x < abcCyr.length; x++)
                if (message.charAt(i) == abcCyr[x]) {
                    builder.append(abcLat[x]);
                }
        }
        return builder.toString().toLowerCase();
    }

    @GetMapping("/getFile/{user}/{id}")
    public void getFile(@PathVariable String user, @PathVariable int id, HttpServletResponse response) {
        User userObj = UserContainer.getUserByName(user);
        if (userObj == null) {
            return;
        }

        Set<UserAudio> userMelodies = userObj.getUserAudio();
        Optional<UserAudio> first = userMelodies.stream().filter(melody -> melody.getId() == id).findFirst();
        if (!first.isPresent()) {
            return;
        }
        UserAudio melody = first.get();

        try {
            File file = findFile(user, melody.getFilename());
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            Files.copy(file.toPath(), response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            throw new RuntimeException("File not found");
        }
    }


    private static File findFile(String user, String name) throws Exception {
        Path path = Paths.get(settings.getFolderUserMusic() + user + "/");

        List<File> list = Files.walk(path)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());

        for (File file : list) {
            if (file.getName().equals(name)) {
                return file;
            }
        }
        throw new FileNotFoundException();
    }

    @PostMapping(value = "/remove")
    public Message remove(HttpServletRequest request, int id) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }


        Optional<UserAudio> first = user.getUserAudio().stream().filter(melody -> melody.getId() == id).findFirst();
        if (!first.isPresent()) {
            return new Message(Error, "No melody by such id");
        }

        UserAudio userAudio = first.get();

        String melodyFilename = userAudio.getFilename();

        //перед удалением проверим не назначена ли мелодия на сценарий
        Set<Rule> allRules = user.getAllRules();
        for (Rule rule : allRules) {
            Integer greetingId = rule.getGreetingId();
            Integer messageId = rule.getMessageId();
            if (greetingId != null && greetingId == id || messageId != null && messageId == id) {
                return new Message(Error, "Melody assigned to scenario '" + rule.getScenario() + "', rule '" + rule.getName()+"'");
            }
        }

        try {
            user.removeUserAudio(userAudio);
            HibernateController.update(user);
            try {
                Files.delete(Paths.get(settings.getFolderUserMusic() + user.getLogin() + File.separator + melodyFilename));
            } catch (NoSuchFileException ignored) {}
            return new Message(Success, "Removed");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Ошибка удаления мелодии", e);
        }
        return new Message(Error, "Internal error");
    }
}