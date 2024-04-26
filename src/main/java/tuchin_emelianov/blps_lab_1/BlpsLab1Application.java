package tuchin_emelianov.blps_lab_1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class BlpsLab1Application {

    public static void main(String[] args) {
        SpringApplication.run(BlpsLab1Application.class, args);
    }

}
