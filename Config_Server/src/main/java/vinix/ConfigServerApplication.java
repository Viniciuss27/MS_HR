package vinix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication /*implements CommandLineRunner*/{

	/*@Value("${GIT_PASSWORD}")
	String pass;
	
	@Value("${GIT_USERNAME}")
	String name;*/
	
	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}
/*
	@Override
	public void run(String... args) throws Exception {
		System.out.println("TOKEN: " + pass != null && !pass.isBlank()));
		System.out.println("USERNAME: " + username != null && !username.isBlank()));
		
	}*/

}
