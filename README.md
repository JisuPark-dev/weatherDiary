# 🌦️ WeatherDiary
## 날씨일기 프로젝트
### 🍀 그 날의 날씨와 함께 일기 작성하기
### 🗓️ 프로젝트 기간: 7.25~ 7.26
### 👨‍💻 사용한 날씨 API : Open Weather Map API

# API 명세서
## ✅ POST / create / diary
- date parameter 로 받아주세요. (date 형식 : yyyy-MM-dd)
- text parameter 로 일기 글을 받아주세요.
- 외부 API 에서 받아온 날씨 데이터와 함께 DB에 저장해주세요.

## ✅ GET / read / diary
- date parameter 로 조회할 날짜를 받아주세요.
- 해당 날짜의 일기를 List 형태로 반환해주세요.

## ✅ GET / read / diaries
- startDate, ednDate parameter 로 조회할 날짜 기간의 시작일/종료일을 받아주세요.
- 해당 기간의 일기를 List 형태로 반환해주세요.

## ✅ PUT / update / diary
- date parameter 로 수정할 날짜를 받아주세요.
- text parameter 로 수정할 새 일기 글을 받아주세요.
- 해당 날짜의 첫번째 일기 글을 새로 받아온 일기글로 수정해주세요.

## ✅ DELETE / delete / diary
- date parameter 로 삭제할 날짜를 받아주세요.
- 해당 날짜의 모든 일기를 지워주세요.

# 🤹‍♂️ 주요 기술 구현
### 1. DB와 관련된 함수들을 트랜잭션 처리
### 2. 캐싱, 스캐줄링 : 매일 새벽 1시에 날씨 데이터를 외부 API 에서 받아다 DB에 저장해두는 로직을 구현
- 요청을 빠르게 처리 가능
- 서버 부하가 줄어듦
- api 사용료 절감 가능
### 3. logback 을 이용하여 프로젝트에 로그 기록
### 4. ExceptionHandler 을 이용한 예외처리
### 5. swagger 을 이용하여 API documentation 작성
### 6. DiaryService의 경우 확장성을 고려하여 인터페이스로 구현함. 