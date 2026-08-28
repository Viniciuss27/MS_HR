package vinix;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication implements CommandLineRunner{

	@Value("${spring.cloud.config.server.git.password}")
	String password;
	
	@Value("${spring.cloud.config.server.git.username}")
	String name;
	
	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		/*System.out.println("TOKEN: " + pass);
		System.out.println("USERNAME: " + name);*/
		
	}

}
