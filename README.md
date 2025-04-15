# 📡 WebSocket 채팅 API 명세

## ✅ WebSocket 연결 정보

- **WebSocket URL**:  
  - 개발: `ws://localhost:8080/ws-chat`  
  - 운영: `wss://{{base-url}}/ws-chat`
- **Protocol**: WebSocket + STOMP (SockJS 지원)
- **Authorization Header**:  
  `Authorization: Bearer {JWT_ACCESS_TOKEN}`

---

## 📤 메시지 전송

- **STOMP Destination**: `/app/chat.send/{peepId}`
- **Payload (JSON)**:
```json
{
  "content": "메시지 본문"
}
```

---

## 📥 메시지 수신

- **STOMP Subscribe**: `/topic/room/{peepId}`
- **Response JSON**:
```json
{
  "sender": "gangjjang5",
  "content": "이 핍 공감돼요 ㅋㅋ",
  "sentAt": "2025-04-15T13:11:00"
}
```

---

## 🔐 인증 방식

- WebSocket 연결 시 JWT 토큰 필요
- 헤더에 다음 형식으로 포함:
  ```
  Authorization: Bearer {accessToken}
  ```

---

## 🧪 전체 사용 예시

1. JWT 로그인 후 토큰 획득
2. WebSocket 연결 (`/ws-chat`)
3. `/topic/room/123` 구독
4. `/app/chat.send.123`로 메시지 전송

---

# 🗂️ REST API 명세서

## 📘 GET /api/chats/{peepId}

특정 게시물(Peep)에 대한 **채팅 메시지 전체 목록을 시간순으로 반환**합니다.

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
      "sender": "gangjjang5",
      "content": "이 핍 공감돼요 ㅋㅋ",
      "sentAt": "2025-04-15T13:11:00"
    },
    {
      "sender": "other_user",
      "content": "진짜 저도요 ㅎㅎ",
      "sentAt": "2025-04-15T13:12:20"
    }
  ],
  "error": null
}
```

### 📒 응답 설명

| 필드      | 설명                 |
|-----------|----------------------|
| sender    | 발신자 ID 또는 닉네임 |
| content   | 채팅 메시지 본문     |
| sentAt    | 보낸 시각 (ISO 포맷) |

---

## 📎 참고 사항

- 이 API는 채팅방에 새로 입장한 사용자가 이전 대화를 로드할 때 사용합니다.
- 읽음 처리나 pagination은 현재 미구현 상태이며, 추후 확장 예정입니다.
