import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { API_BASE_URL } from "@/lib/api/api-base-url";

// 백엔드 jwt.cookie.access-token-name, refresh-token-name과 같아야 한다
const ACCESS_TOKEN_COOKIE = "AT";
const REFRESH_TOKEN_COOKIE = "RT";

/**
 * 페이지를 그리기 전에 액세스 토큰을 갱신한다.
 *
 * 페이지는 서버에서 브라우저 쿠키를 그대로 백엔드에 넘겨 데이터를 받는다(server-request-options.ts).
 * 액세스 토큰 쿠키는 토큰이 만료될 때 브라우저에서 함께 사라져서, 그 뒤 처음 연 페이지는 비로그인 데이터로 그려진다.
 * 브라우저의 401 갱신(axios-instance.ts)은 페이지가 그려진 뒤에 일어나서 새로고침해야 반영된다.
 * 갱신한 새 쿠키는 이번 렌더링 요청과 브라우저 응답에 함께 넣는다.
 */
export async function proxy(request: NextRequest) {
    // 쿠키 수명이 토큰 수명과 같으므로 액세스 토큰 쿠키가 있으면 아직 유효하다고 본다
    if (request.cookies.has(ACCESS_TOKEN_COOKIE) || !request.cookies.has(REFRESH_TOKEN_COOKIE)) {
        return NextResponse.next();
    }

    const setCookies = await refreshTokens(request);
    if (!setCookies) {
        return NextResponse.next();
    }

    // 이번 렌더링이 새 토큰으로 백엔드를 부르게 한다
    for (const setCookie of setCookies) {
        const [name, value] = parseSetCookie(setCookie);
        request.cookies.set(name, value);
    }
    const response = NextResponse.next({ request: { headers: request.headers } });

    // 쿠키 속성(도메인, 만료 등)은 백엔드가 정한 그대로 브라우저에 넘긴다
    for (const setCookie of setCookies) {
        response.headers.append("set-cookie", setCookie);
    }

    return response;
}

/**
 * 토큰을 갱신하고 백엔드가 준 Set-Cookie 값들을 돌려준다.
 *
 * 실패하면 null이고, 페이지는 비로그인으로 그려진다.
 * 실패해도 리프레시 토큰 쿠키는 지우지 않는다.
 * 동시에 온 다른 요청이 먼저 토큰을 바꿨다면, 지우는 쿠키가 그 요청이 받은 새 토큰을 지워 로그아웃된다.
 * 같은 까닭으로 타임아웃도 두지 않는다.
 * 백엔드가 리프레시 토큰을 바꾼 뒤 응답을 버리면 새 토큰을 잃는다.
 */
async function refreshTokens(request: NextRequest): Promise<string[] | null> {
    const headers = new Headers({ cookie: request.headers.get("cookie") ?? "" });

    // 백엔드가 새 리프레시 토큰에 남기는 IP가 프론트 서버가 아니라 사용자 IP가 되게 한다
    const forwardedFor = request.headers.get("x-forwarded-for");
    if (forwardedFor) {
        headers.set("x-forwarded-for", forwardedFor);
    }

    try {
        const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
            method: "POST",
            headers,
            cache: "no-store",
        });
        return response.ok ? response.headers.getSetCookie() : null;
    } catch {
        return null;
    }
}

// "AT=값; Path=/; ..."에서 이름과 값만 꺼낸다
function parseSetCookie(setCookie: string): [string, string] {
    const pair = setCookie.split(";", 1)[0];
    const separatorIndex = pair.indexOf("=");
    return [pair.slice(0, separatorIndex).trim(), pair.slice(separatorIndex + 1).trim()];
}

export const config = {
    matcher: [
        {
            // 정적 파일과 이미지 최적화 요청은 페이지가 아니라서 뺀다
            source: "/((?!_next/static|_next/image|.*\\.).*)",
            // 미리 받기 요청은 응답이 버려질 수 있어서 토큰을 바꾸지 않는다.
            // 동적 페이지는 미리 받지 않아서 비로그인 데이터가 미리 받아지지도 않는다.
            missing: [
                { type: "header", key: "next-router-prefetch" },
                { type: "header", key: "purpose", value: "prefetch" },
            ],
        },
    ],
};
