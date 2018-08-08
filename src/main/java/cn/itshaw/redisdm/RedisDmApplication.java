package cn.itshaw.redisdm;

import com.didispace.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSwagger2Doc
public class RedisDmApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisDmApplication.class, args);
	}
}
