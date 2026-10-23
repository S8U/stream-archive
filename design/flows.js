// Stream Archive 웹의 화면 목록과 흐름. screen-design 스킬의 design.py가 읽고, 흐름 보기가 이 파일대로 그린다.
// 형식과 규칙은 screen-design 스킬의 references/rules.md에 있다. 여기에는 이 앱에만 해당하는 설명만 적는다.
//
// - 웹 앱 하나라 플랫폼은 web뿐이다. 넓은 창(web-, md 이상)과 휴대폰 브라우저(mweb-)는 sheets로 나눈다.
// - 색, 크기, 글꼴은 frontend/src/app/globals.css 토큰과 components/ui(shadcn) 클래스 값을 옮겼다. 디자인은 무채색이고 악센트 색을 쓰지 않는다.
// - 화면 설계는 2026-10-22 「일부공개 보기」 개인 설정을 만들며 처음 그렸다. 그래서 그 기능이 닿는 화면만 있다.
//   - 관리자 전용 설정 두 개를 내 정보 > 설정 탭에 둔다. 처음에는 하나였는데 사용자가 채널과 동영상을 나누자고 했다(같은 날).
//     설정 하나가 공개범위 조건 하나만 바꾼다. 두 조건은 함께 걸려서, 일부공개 채널의 일부공개 동영상은 둘 다 켜야 보인다.
//     - 일부공개 채널 보기: 사이드바 채널 목록, 그리고 홈·검색에서 그 채널의 동영상까지 보인다.
//     - 일부공개 동영상 보기: 홈·검색, 채널 상세 동영상 목록에서 일부공개 동영상이 보인다.
//   - 비공개(PRIVATE)는 계속 관리 페이지에서만 보인다. 시청 기록과 관리 페이지, 주소로 여는 권한은 바뀌지 않는다.
//   - 일부공개 표시는 그 대상 옆에 링크 아이콘으로 붙인다. 채널은 이름 뒤(사이드바, 카드의 채널 줄), 동영상은 카드 제목 뒤(소장 아이콘 앞)다.
//     2026-10-23 사용자 요청으로 동영상의 「일부공개」 글자를 빼고 제목 뒤 아이콘으로 옮겼다. 아이콘이 커 보여서 채널 12px, 동영상 13px(소장 아이콘과 같다)로 줄였다.
//     채널 상세와 검색 결과는 홈과 같은 카드라 따로 그리지 않았다.
//   - 일반 사용자에게는 설정 탭이 없다. 지금 있는 설정이 모두 관리자 전용이라서다.
// - 웹은 자동 캡처가 없어 captures가 비어 있다.
window.DESIGN = {
  captures: {},

  screens: {
    "web-home": { title: "홈: 일부공개 채널·동영상 보기 켬", w: 1440, h: 900 },
    "web-profile-settings": { title: "내 정보: 설정 탭 (관리자)", w: 1440, h: 900 },
    "web-profile": { title: "내 정보: 일반 사용자 (설정 탭 없음)", w: 1440, h: 900 },
    "mweb-home": { title: "휴대폰: 홈, 일부공개 채널·동영상 보기 켬", w: 393, h: 852 },
    "mweb-sidebar": { title: "휴대폰: 사이드바 열림", w: 393, h: 852 },
    "mweb-profile-settings": { title: "휴대폰: 내 정보 설정 탭 (관리자)", w: 393, h: 852 }
  },

  flow: [
    { id: "home", title: "홈", web: { screen: "web-home" } },
    { id: "profile", from: "home", action: "아바타 › 내 정보 › 설정 탭", title: "내 정보",
      web: { screen: "web-profile-settings" },
      states: [{ title: "일반 사용자", web: { screen: "web-profile" } }] }
  ],

  sheets: [
    { id: "wide", title: "넓은 창" },
    { id: "phone", title: "휴대폰", flow: {
      home: { web: { screen: "mweb-home" },
              states: [{ title: "사이드바 열림", web: { screen: "mweb-sidebar" } }] },
      profile: { web: { screen: "mweb-profile-settings" } }
    } }
  ],

  compare: []
};
