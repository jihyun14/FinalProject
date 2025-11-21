package com.prinCipal.chatbot.content;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController; // @Controller 대신 사용

// 👇 다른 패키지에 있는 클래스들이라 import가 꼭 필요합니다!
import com.prinCipal.chatbot.admin.Lawyer;
import com.prinCipal.chatbot.admin.LawyerService;

@RestController
public class LawyerUserController {

    // 1. 서비스 변수 선언 (final 권장)
    private LawyerService lawyerService;

    // 2. 생성자 주입 (Spring이 자동으로 LawyerService를 연결해줌)
    public LawyerUserController(LawyerService lawyerService) {
        this.lawyerService = lawyerService;
    }

    @GetMapping("/api/lawyers")
    public ResponseEntity<List<Lawyer>> getAllLawyers() {
        // 이제 lawyerService 변수를 사용할 수 있습니다.
        List<Lawyer> lawyers = lawyerService.getAllLawyers();
        return ResponseEntity.ok(lawyers);
    }
}