package com.czetsuyatech.nerv.example.exception;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class NervExceptionDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(NervExceptionDemoApplication.class, args);
  }

}
