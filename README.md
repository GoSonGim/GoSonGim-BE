# 또바기 (Ttobaki) Backend API


> **또바기**는 AI 아바타와의 대화를 통해 발화 및 의사소통 재활을 돕는 서비스입니다.      
> 사용자는 조음 키트를 통한 단어 발음 연습과 상황극을 통한 대화 연습을 할 수 있습니다.

 **🌐 배포 주소**: [https://ttobaki.app](https://ttobaki.app/)
 
<img width="1920" height="1080" alt="표지" src="https://github.com/user-attachments/assets/87ad4c2c-1ff4-4340-b2f4-010959cff3a0" />

---

### 👥 개발 정보

#### 1. 개발 기간
- **시작일**: 2025-10-09
- **종료일**: 2025-11-27
- **총 개발 기간**: 약 7주

#### 2. 백엔드 팀 구성 (2인)
- **김다연**:
  - 인증 시스템 구축 (JWT 토큰 기반 인증, Google OAuth 2.0 소셜 로그인)
  - 상황극 학습 도메인 개발 (OpenAI GPT 통합, HeyGen AI 대화 세션 관리)
  - 복습 기능 구현 (일별/월별 학습 기록, 조음 키트 복습, 상황극 복습)
  - OpenAI Whisper 기반 STT(음성 인식) 기능 구현
  - 이미지 업로드 API 및 S3 파일 관리
  - Docker Compose 배포 설정 및 인프라 구성

- **고여경**:
  - 사용자 관리 API 개발 (사용자 정보 조회/수정, 학습 기록 조회)
  - 조음 키트 도메인 개발 (발음 평가, 진단 기능, 복습 조회)
  - 북마크 기능 구현
  - 사용자 학습 통계 API (연속 학습 일수, 상황극 통계)
  - 음성 파일 업로드 S3 파일 관리
  - CI/CD 파이프라인 구축
    
---

### 🛠 기술 스택

- #### Backend
  - **Java** 17
  - **Spring Boot** 3.5.6 (Spring Security, Spring Data JPA 포함)
  - **JWT** 0.12.3
  - **Gradle** 8.5

- #### Database & Cache
  - **MySQL** 8.0
  - **Redis** 7.0

- #### External Services
  - **Azure Speech Service** SDK 1.43.0 (발음 평가, STT)
  - **OpenAI GPT** (상황극 대화 생성)
  - **HeyGen** (AI 아바타 비디오 생성)
  - **AWS S3** SDK 2.27.7 (파일 스토리지)
  - **Google OAuth 2.0** Client 2.2.0 (소셜 로그인)

- #### DevOps & Tools
  - **Docker** & **Docker Compose** (컨테이너화)
  - **Nginx** (리버스 프록시)
  - **SpringDoc OpenAPI** 2.7.0 (API 문서화)

---

### 🎯 프로젝트 요약

- **실시간 발음 평가**: Azure Speech SDK를 활용한 정확한 발음 평가
- **AI 기반 상황극 학습**: OpenAI GPT를 활용한 맞춤형 대화 연습
- **AI 아바타 비디오**: HeyGen을 활용한 상황극 학습용 AI 비디오 생성
- **학습 진도 관리**: 사용자별 학습 기록 및 진도 추적
- **소셜 로그인**: Google OAuth 2.0을 통한 간편 로그인
- **RESTful API**: 표준화된 REST API 설계

---

### ✨ 주요 기능

#### 1. 인증 및 사용자 관리
- 이메일 기반 회원가입/로그인
- Google OAuth 2.0 소셜 로그인
- JWT 기반 토큰 인증 (Access Token + Refresh Token)
- 로그아웃 및 토큰 무효화

#### 2. 조음 키트 학습
- 카테고리별 조음 키트 목록 조회
- 단어별 발음 평가 (Azure Speech SDK)
- 발음 정확도, 유창성, 완성도, 운율 점수 제공
- 학습 기록 저장 및 진도 관리
- 진단 평가를 통한 키트 추천

#### 3. 상황극 학습
- 카테고리별 상황극 목록 조회
- OpenAI GPT 기반 대화 세션 생성
- HeyGen AI 아바타를 활용한 비디오 생성
- 사용자 답변 평가 및 다음 질문 생성
- 음성 인식(STT)을 통한 답변 입력
- 학습 세션 기록 저장

#### 4. 학습 관리
- 북마크 기능 (키트/상황극)
- 사용자 학습 통계 (연속 학습 일수, 총 성공 횟수)
- 학습 레벨 시스템
- 학습 기록 조회

#### 5. 파일 관리
- AWS S3를 통한 이미지 업로드
- Presigned URL을 통한 안전한 파일 업로드

---

### 📚 API 문서

**[API 명세서](https://danhandev.notion.site/API-2c6b74c442528005abf2e0986114c551?source=copy_link)**

---

### 🏗 시스템 아키텍처

<img width="4234" height="2199" alt="Blank diagram - Page 1 Ttobaki Backend System Architecture" src="https://github.com/user-attachments/assets/545bc999-c5af-4189-935f-66dda1e16e64" />

---

### 🗄 데이터베이스 구조

<img width="982" height="848" alt="erd" src="https://github.com/user-attachments/assets/fe2843d8-f485-4631-88e8-3293d7c35389" />


---

### 📸 서비스 화면


https://github.com/user-attachments/assets/3a89d6c0-3223-41b8-a6e0-f2f680b4c663



---

<p align="center">
  <strong>또박이와 함께 자신감 있는 말하기를 시작하세요! 🎉</strong>
</p>
