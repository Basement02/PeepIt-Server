# 📡 WebSocket 채팅 API 명세

## ✅ WebSocket 연결 정보

- **WebSocket 연결 API**:  
  - 개발: `http://localhost:8080/ws-chat`  
  - 운영: `http://{{base-url}}/ws-chat`
- **Protocol**: WebSocket + STOMP (SockJS 지원)
- **Authorization Header**: 필요 없음

---

## 📤 메시지 전송

- **STOMP Destination**: `/pub/chat.send.{peepId}`
- **Payload (JSON)**:
```json
{
  "peepId": 1, // peep 고유 ID (Long)
  "uid": "사용자ID입니다",
  "content": "채팅본문입니다"
}
```

---

## 📥 메시지 수신

- **STOMP Subscribe**: `/sub/chat.receive.{peepId}`
- **Response JSON**:
```json
{
  "peepId": 1, // peep 고유 ID (Long)
  "nickname": "닉네임입니다",
  "imgUrl": "imgURL입니다",
  "content": "안녕하세요!",
  "sendAt": "방금 전"
}
```

---

## 🔐 인증 방식

- WebSocket 연결 시 JWT 토큰 불필요
- Request DTO의 user id가 유효한지만 서버에서 검증

---

## 🧪 전체 사용 예시

1. WebSocket 연결 (`/ws-chat`)
2. `/sub/chat.receive.123` 구독
3. `/pub/chat.send.123`으로 메시지 전송

---

# 🗂️ REST API 명세서

## 📘 GET /api/chats/{peepId}

특정 게시물(Peep)에 대한 **채팅 메시지 전체 목록을 오래된순으로 반환**합니다.

### ✅ Endpoint

```
GET /api/chats/{peepId}
```

### 🔐 Headers

```
Authorization: Bearer {JWT_ACCESS_TOKEN}
```

### 📥 Path Variables

| 이름     | 타입   | 설명                  |
|----------|--------|-----------------------|
| peepId   | Long   | 대상 Peep(게시물) ID |

### 📤 Response Body

```json
{
  "success": true,
  "data": [
    {
      "uid": "gangjjang5",
      "nickname": "이 핍 공감돼요 ㅋㅋ",
      "imgUrl": "https://blah",
      "content": "채팅본문입니다",
      "sentAt": "5분 전"
    },
    {
      "uid": "gangjjang5",
      "nickname": "이 핍 공감돼요 ㅋㅋ",
      "imgUrl": "https://blah",
      "content": "채팅본문입니다",
      "sentAt": "3분 전"
    },
    {
      "uid": "gangjjang5",
      "nickname": "이 핍 공감돼요 ㅋㅋ",
      "imgUrl": "https://blah",
      "content": "채팅본문입니다",
      "sentAt": "방금 전"
    }
  ],
  "error": null
}
```

## 📎 참고 사항

- 이 API는 채팅방에 새로 입장한 사용자가 이전 대화를 로드할 때 사용합니다.
- 추후 채팅 조회는 페이지네이션 기능을 추가하여 확장 예정입니다.
