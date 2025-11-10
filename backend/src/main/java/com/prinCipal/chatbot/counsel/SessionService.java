package com.prinCipal.chatbot.counsel;

import com.prinCipal.chatbot.ChatService; // 👈 1. ChatService 임포트
import com.prinCipal.chatbot.Content.CounsellingContent;
import com.prinCipal.chatbot.Content.CounsellingContentRepository;
import com.prinCipal.chatbot.Content.Sender;
import com.prinCipal.chatbot.dto.ChatRequestDto; // 👈 2. ChatRequestDto 임포트
import com.prinCipal.chatbot.dto.ChatResponseDto; // 👈 3. ChatResponseDto 임포트
import com.prinCipal.chatbot.dto.FastApiRequestDto;
import com.prinCipal.chatbot.dto.FastApiResponseDto;
import com.prinCipal.chatbot.member.Member;
import com.prinCipal.chatbot.member.MemberRepository; // (가상) Member 조회를 위해
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CounsellingContentRepository contentRepository;
    private final ChatService chatService; // 👈 4. ChatService 주입
    private final MemberRepository memberRepository; // (가상) Member 조회를 위해
    private final RestTemplate restTemplate;
    
    // FastAPI 엔드포인트 주소 (docker-compose의 서비스 이름 사용)
    private final String FASTAPI_URL = "http://fastapi:8000/generate-response";
    
    /**
     * 새 상담 세션 생성
     */
    public CounsellingSession createSession(Member member) {
        CounsellingSession session = CounsellingSession.builder()
                .member(member)
                .startTime(LocalDateTime.now()) // CounsellingSession 빌더 수정 필요
                .completionStatus(CompletionStatus.ONGOING)
                .build();
        
        return sessionRepository.save(session);
    }

    /**
     * 세션 삭제 (소유권 확인)
     */
    public void deleteSession(Long sessionId, Member member) {
        CounsellingSession session = sessionRepository.findBySessionIdAndMember(sessionId, member)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없거나 권한이 없습니다."));
        
        sessionRepository.delete(session);
    }

    /**
     * 메시지 저장 및 봇 응답 처리 (ChatRequestDto를 직접 사용)
     */
    public ChatResponseDto addMessage(ChatRequestDto requestDto) {
        
        // 1. DTO에서 ID 추출
        Long sessionId = requestDto.getSessionId().longValue();
        Long memberId = requestDto.getUserId().longValue();

        // 2. 세션 및 사용자 엔티티 조회 (소유권 확인)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        CounsellingSession session = sessionRepository.findBySessionIdAndMember(sessionId, member)
                .orElseThrow(() -> new RuntimeException("세션을 찾을 수 없거나 권한이 없습니다."));

        // 3. 프론트에서 받은 사용자 메시지(PERSON) DB에 저장
        CounsellingContent userMessage = CounsellingContent.builder()
                .session(session)
                .sender(Sender.PERSON)
                .content(requestDto.getUserMessage())
                .build();
        contentRepository.save(userMessage);

        // 4. (중요) 기존 ChatService 호출
        //    (ChatService는 이미 FastApiRequestDto/ResponseDto를 사용)
        ChatResponseDto botResponse = chatService.getFastApiResponse(requestDto); //
        String botReplyText = botResponse.getText();

        // 5. 봇 메시지(CHATBOT) DB에 저장
        CounsellingContent botMessage = CounsellingContent.builder()
                .session(session)
                .sender(Sender.CHATBOT)
                .content(botReplyText)
                .build();
        contentRepository.save(botMessage);

        // 6. 프론트엔드로 봇 응답 반환
        return botResponse;
    }
    
}