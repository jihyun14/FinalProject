package com.prinCipal.chatbot.counsel;

import com.prinCipal.chatbot.counsel.CounsellingSession;
import com.prinCipal.chatbot.counsel.SessionService;
import com.prinCipal.chatbot.dto.ChatRequestDto; // 👈 1. 프론트에서 받을 DTO
import com.prinCipal.chatbot.dto.ChatResponseDto; // 👈 2. 프론트로 보낼 DTO
import com.prinCipal.chatbot.counsel.SessionCreationResponse; // (세션 생성 DTO)
import com.prinCipal.chatbot.member.Member;
import com.prinCipal.chatbot.member.MemberService; // (가상) 현재 사용자 정보를 가져오는 서비스
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SessionController {
    
    private final SessionService sessionService;
    private final MemberService memberService; // (가상) 주입 필요

    // 1. GET /api/chats (api.js의 getInitialData()가 호출)
    @GetMapping("/chats")
    public ResponseEntity<?> getInitialData() {
        // ... (이전과 동일)
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("recents", Collections.emptyList());
        initialData.put("sessions", Collections.emptyMap());
        initialData.put("userId", null);
        return ResponseEntity.ok(initialData); 
    }
    
    // 2. POST /api/sessions (api.js의 createSession()이 호출)
    @PostMapping("/sessions")
    public ResponseEntity<?> createSession(@PathVariable Long id) {
        Member currentMember = memberService.FindById(id); // (가상) 현재 사용자
        CounsellingSession newSession = sessionService.createSession(currentMember);

        SessionCreationResponse response = SessionCreationResponse.builder()
                .id(newSession.getSessionId())
                .title("새 대화") 
                .messages(Collections.emptyList())
                .build();
        return ResponseEntity.ok(response);
    }
    
    // 3. POST /api/sessions/{id}/messages (api.js의 saveMessage()가 호출)
    // ⭐️ @RequestBody를 ChatRequestDto로 변경
    @PostMapping("/sessions/{id}/messages")
    public ResponseEntity<ChatResponseDto> saveMessage(
            @PathVariable Long id,
            @RequestBody ChatRequestDto requestDto) { 

        // (보안 강화) URL의 id와 DTO의 id가 일치하는지,
        // 현재 로그인한 사용자가 DTO의 userId가 맞는지 확인하는 로직이 필요합니다.
        
        // 4. Service는 DTO를 그대로 전달받아 처리
        ChatResponseDto botResponse = sessionService.addMessage(requestDto);

        return ResponseEntity.ok(botResponse);
    }

    // 4. DELETE /api/sessions/{id} (api.js의 deleteSession()이 호출)
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        Member currentMember = memberService.FindById(id); // (가상) 현재 사용자
        sessionService.deleteSession(id, currentMember);
        return ResponseEntity.ok(Map.of("message", "Session deleted successfully"));
    }
}