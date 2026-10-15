package com.hotel.operations;

import java.time.OffsetDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hotel.operations.domain.AppUser;
import com.hotel.operations.repository.AppUserRepository;

@SpringBootApplication
public class OperationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(OperationsApplication.class, args);
	}

	//  @Bean
    // public CommandLineRunner seedSystemAdmin(AppUserRepository userRepo, PasswordEncoder encoder) {
    //     return args -> {
    //         String targetEmail = "admin@gmail.com";
            
    //         // Delete old entries to guarantee a clean slate
    //         userRepo.findByEmail(targetEmail).ifPresent(userRepo::delete);
            
    //         // Create a fresh admin user with native system encryption
    //         AppUser admin = new AppUser();
    //         admin.setUserRole("ADMIN");
    //         admin.setFullName("Salim Administrator");
    //         admin.setEmail(targetEmail);
    //         admin.setPasswordHash(encoder.encode("admin123")); // Natively generates clean hash
    //         admin.setIsActive(1);
    //         admin.setCreatedAt(OffsetDateTime.now());
            
    //         userRepo.save(admin);
    //         System.out.println(">>> System Admin Account Natively Re-Seeded Successfully! <<<");
    //     };
    // }
}
