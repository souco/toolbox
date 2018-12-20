package cc.souco.toolbox.soucot;

import com.beust.jcommander.internal.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

@RestController()
@RequestMapping(value = "/soucot")
public class SoucotController {

    private Logger logger = LoggerFactory.getLogger(SoucotController.class);

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public Map test(HttpServletRequest request, String username, Integer age, String message) {
        Map<Object, Object> data = Maps.newHashMap();
        data.put("user", username);
        data.put("age", age);
        data.put("msg", message);
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            data.put(header, request.getHeader(header));
        }
        return data;
    }

    @RequestMapping(value = "/test2", method = RequestMethod.GET)
    public Map test2(String username, Integer age, String message) {
        Map<Object, Object> data = Maps.newHashMap();
        data.put("user", username);
        data.put("age", age);
        data.put("msg", message);

        return data;
    }
}
