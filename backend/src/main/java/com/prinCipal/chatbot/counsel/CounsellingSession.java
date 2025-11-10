package com.prinCipal.chatbot.counsel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;

import com.prinCipal.chatbot.Content.CounsellingContent;
import com.prinCipal.chatbot.alert.CrisisAlert;
import com.prinCipal.chatbot.member.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="CounsellingSession")
@Getter
@NoArgsConstructor
public class CounsellingSession {

	 // 세션 ID
	 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	 private Long sessionId;
	 
	 // 사용자 ID
	 @ManyToOne(fetch = FetchType.LAZY)
	 @JoinColumn(name="user_id")
	 private Member member;
	 
	 // 상담 시작, 상담 끝, 사용자 마지막 메세지
	 @Column(nullable = false)
	 private LocalDateTime startTime;
	 private LocalDateTime endTime;
	 private LocalDateTime lastMessageTime;
	 
	 // 세션 길이
	 @Formula("TIMESTAMPDIFF(SECOND,start_time,end_time)")
	 private Integer durationSec;
	 
	 // 상담 진행 상태 (시작, 중간, 종료)
	 @Enumerated(EnumType.STRING)
	 private CompletionStatus completionStatus = CompletionStatus.ONGOING;
	 
	 // 상담 제목
	 @Column(columnDefinition = "TEXT")
	 private String summaryTitle;
	 
	 // 상담 내용 요약
	 @Column(columnDefinition = "TEXT")
	 private String summary;
	 
	 // 세션 재개용 토큰
	 private String resumeToken;
	 
	 // 미전 대화 맥락 저장 (AI 전달용)
	 @Column(columnDefinition = "JSON")
	 private String contextSnapshot;
	 
	 // 세션 생성일
	 @CreationTimestamp
	 private LocalDateTime createdAt;

	 // 세션 수정일
	 @UpdateTimestamp
	 private LocalDateTime updatedAt;
	 
	 // 세선 1 : 발화 N 관계
	 @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
	 private List<CounsellingContent> contents = new ArrayList<>();
	 
	 // 세션 1 : 감정 N 관계
	 @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
	 private List<EmotionAnalysis> emotionAnalyses = new ArrayList<>();
	 
	 // 세션 1 : 위기알림 N 관계
	 @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
	 private List<CrisisAlert> crisisAlerts = new ArrayList<>();
	 
	 @Builder
	 public CounsellingSession(Member member, LocalDateTime startTime, CompletionStatus completionStatus,String summary,String resumeToken,String contextSnapshot) {
		 this.member = member;
		 this.startTime = startTime;
		 this.completionStatus = completionStatus;
		 this.summary = summary;
		 this.contextSnapshot =contextSnapshot;
		 this.resumeToken = resumeToken;
	}
	
	 public void updateStatus(CompletionStatus completionStatus) {
		 this.completionStatus = completionStatus;
	 }
	 
	 
	 
}








