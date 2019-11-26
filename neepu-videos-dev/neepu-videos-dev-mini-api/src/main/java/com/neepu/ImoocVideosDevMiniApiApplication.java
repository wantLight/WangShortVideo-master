package com.neepu;

import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(basePackages="com.neepu.mapper")
@ComponentScan(basePackages= {"com.neepu", "org.n3r.idworker"})
public class ImoocVideosDevMiniApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImoocVideosDevMiniApiApplication.class, args);
	}
}
