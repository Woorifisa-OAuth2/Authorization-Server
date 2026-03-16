# OAuth 2.0 Authorization Server
## **1. 프로젝트 개요**

이 프로젝트는 **Spring Authorization Server**를 기반으로 구현한 **OAuth2 / OIDC 인가 서버입니다.**

<img width="2094" height="1139" alt="Image" src="https://github.com/user-attachments/assets/720e241d-2fc2-4ccc-979b-12b6471382e1" />

- 사용자 로그인 인증 처리
- 클라이언트 애플리케이션 동적 등록
- Authorization Code 발급
- Access Token / Refresh Token / ID Token 발급
- JWT Access Token에 사용자 추가 정보(user_id, name, age, gender, email 등) 커스터마이징
- DB 기반 사용자 및 클라이언트 관리

정적 클라이언트 하나만 등록하는 예제가 아니라,

**여러 리소스 서버(클라이언트)가 인가 서버에 등록 요청을 보내고, 발급받은 client_id / client_secret으로 OAuth2 인증 흐름을 수행할 수 있는 구조**를 구축했습니다.

---

# **2. 단계별 상세 흐름**

### **1) 클라이언트 등록**

리소스 서버 또는 외부 클라이언트 애플리케이션은 인가 서버에 등록 요청을 보낸다.

```json
{
  "clientName": "resource-server-1",
  "redirectUri": "http://127.0.0.1:8081/login/oauth2/code/resource-server-1",
  "scopes": ["openid", "read", "write"]
}
```

인가 서버는 다음 정보를 생성해 DB에 저장한다.

- client_id
- client_secret (암호화 저장)
- redirect_uri
- scopes
- grant_types
- client_auth_methods

응답으로는 **평문 client_secret**을 한 번만 반환한다.

```json
{
  "clientId": "발급된 client id",
  "clientSecret": "평문 secret"
}
```

---

### **2) 인가 요청**

클라이언트는 사용자를 인가 서버로 리다이렉트한다.

```json
GET /oauth2/authorize
    ?response_type=code
    &client_id=...
    &redirect_uri=...
    &scope=openid read write
    &state=...
```

이 단계에서 인가 서버는 다음을 확인한다.

- 해당 client_id가 등록된 클라이언트인지
- redirect_uri가 등록된 URI와 일치하는지
- 요청한 scope가 허용된 범위인지

---

### **3) 로그인 페이지로 리다이렉트**

사용자가 아직 인증되지 않았다면, 인가 서버는 /login 페이지로 리다이렉트한다.

즉 흐름은 다음과 같다.

```json
/oauth2/authorize 요청
→ 인증 필요
→ /login 리다이렉트
```

---

### **4) 사용자 로그인**

사용자는 인가 서버가 제공하는 로그인 페이지에서 아이디/비밀번호를 입력한다.

로그인 성공 시 Spring Security는 원래 요청했던 /oauth2/authorize로 다시 복귀시킨다.

---

### **5) 권한 동의 화면**

클라이언트 설정에서 requireAuthorizationConsent(true)가 활성화되어 있으므로,

로그인 후 인가 서버는 사용자에게 권한 동의 화면을 보여준다.

예를 들어 다음과 같은 scope를 승인받는다.

- openid
- read
- write

---

### **6) Authorization Code 발급**

사용자가 승인하면 인가 서버는 **Authorization Code**를 생성하고, 등록된 redirect_uri로 리다이렉트한다.

- 이때 브라우저는 단순히 code를 전달하는 역할을 하며, 실제 토큰 교환은 클라이언트 애플리케이션이 수행한다.

---

### **7) Token 발급 요청**

클라이언트는 발급받은 code를 사용해 인가 서버의 /oauth2/token 엔드포인트에 요청한다.

```json
grant_type=authorization_code
client_id=...
client_secret=...
redirect_uri=...
code=...
```

인가 서버는 다음을 검증한다.

- client_id, client_secret 유효성
- code 유효성 및 1회 사용 여부
- redirect_uri 일치 여부

검증이 완료되면 토큰을 발급한다.

---

### **8) 최종 발급 토큰**

인가 서버는 다음 토큰을 반환한다.

- access_token
- refresh_token
- id_token (OIDC 활성화 시)

응답 예시:

```json
{
  "access_token": "...",
  "refresh_token": "...",
  "id_token": "...",
  "token_type": "Bearer",
  "expires_in": 299,
  "scope": "read openid"
}
```

---

# **3. 프로젝트 핵심 구성 요소**

## **3-1. AuthorizationServerConfig**

인가 서버의 핵심 엔드포인트를 활성화하는 설정 클래스입니다.

```java
OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
```

OAuth2AuthorizationServerConfigurer를 HttpSecurity에 붙여서Authorization Server 전용 필터들과 엔드포인트 처리를 등록

내부적으로 다음 엔드포인트들을 활성화합니다.

- /oauth2/authorize
- /oauth2/token
- /oauth2/jwks
- /oauth2/revoke
- /oauth2/introspect
- OIDC 관련 엔드포인트

즉, **Authorization Code 발급과 Token 발급 로직은 직접 Controller를 구현한 것이 아니라 Spring Authorization Server 내부 필터와 Provider가 처리**

---

## **3-2. JpaUserDetailsService**

사용자 정보를 DB에서 조회하여 Spring Security의 UserDetails로 변환하는 클래스이다.

- DB에서 사용자 조회
- Spring Security 인증용 UserDetails 생성

---

## 3-3. **TokenCustomizerConfig**

JWT Access Token / ID Token에 사용자 정보를 추가하는 커스터마이저입니다.

- Access Token에 user_id, username, role, name, age, gender, email 추가
- ID Token에 user_id, username 추가

이 Bean은 직접 호출하지 않아도 된다.
Spring Authorization Server가 JWT 생성 시점에 자동으로 찾아 실행합니다.

<aside>
💡

/oauth2/token 요청
→ Spring Authorization Server 내부 JWT 생성
→ OAuth2TokenCustomizer 실행
→ Claim 추가
→ 최종 JWT 발급

</aside>

→ 리소스 서버는 Access Token만 디코딩해도 사용자 식별에 필요한 정보를 바로 꺼낼 수 있도록 했습니다.

---

## **3-4. ClientRegistrationService**

외부 클라이언트를 동적으로 등록하는 서비스입니다.

- client_id 생성
- 평문 client_secret 생성
- 암호화 후 DB 저장
- OAuth2 Client 설정 생성
- 등록 완료 후 평문 secret 응답

---

## **3-5. JpaRegisteredClientRepository**

등록된 클라이언트를 DB에 저장하고 다시 RegisteredClient로 복원하는 저장소 구현체입니다.

- RegisteredClient → OAuthClient 엔티티 변환 후 저장
- DB 조회 후 OAuthClient → RegisteredClient 복원

→ 이 구현체를 통해 **메모리 기반이 아닌 DB 기반 동적 클라이언트 관리**가 가능해지도록 구현했습니다.
