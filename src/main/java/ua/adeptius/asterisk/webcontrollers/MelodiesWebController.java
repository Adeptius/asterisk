package ua.adeptius.asterisk.webcontrollers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping(value = "/melodies", produces = "application/json; charset=UTF-8")
@ResponseBody
public class MelodiesWebController {


    private static String UPLOADED_FOLDER = "D:\\temp\\";

    @ResponseBody
    @PostMapping("/upload")
    public String singleFileUpload(HttpServletRequest request,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam("name") String name,
                                   RedirectAttributes redirectAttributes) {

        System.out.println("Authorization " + request.getHeader("Authorization"));
        System.out.println("NAME IS " + name);
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "NO FILE!!!!!!!";
        }

        try {

            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);

            redirectAttributes.addFlashAttribute("message", "You successfully uploaded '" + file.getOriginalFilename() + "'");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Succsess!!!!!!!!!!!!";
    }


}
