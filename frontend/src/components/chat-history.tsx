'use client';

import { useEffect, useRef, useState, useCallback } from 'react';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from "@/components/ui/separator";
import { getVideoChatHistory } from '@/lib/api/endpoints/video/video';
import { ChatHistoryResponse } from "@/lib/api/models/chatHistoryResponse";

interface ChatHistoryProps {
    videoUuid: string;
    currentTimeMs: number;
}

// 시간 포맷팅 (ms -> MM:SS)
function formatTime(milliseconds: number): string {
    const seconds = Math.floor(milliseconds / 1000);
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    if (hours > 0) {
        return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
}

// 닉네임 색깔 (닉네임 기반 랜덤)
function getUsernameColor(username: string): string {
    const colors = [
        'text-blue-500',
        'text-green-500',
        'text-purple-500',
        'text-pink-500',
        'text-orange-500',
        'text-cyan-500',
        'text-yellow-600',
        'text-red-500',
        'text-indigo-500',
        'text-rose-500',
    ];

    // 닉네임 각 문자의 코드에 위치를 곱해서 더하기
    let hash = 0;
    for (let i = 0; i < username.length; i++) {
        hash += username.charCodeAt(i) * (i + 1);
    }

    const index = hash % colors.length;
    return colors[index];
}


// 한번에 불러올 채팅 시간 (ms)
const CHAT_LOAD_MILLIS = 3000;
// 건너뛰었을 때 불러올 이전 채팅 시간 (ms)
const LOAD_BEFORE_CHAT_MILLIS = 60000;
// 채팅 최대 개수
const MAX_NUMBER_OF_CHAT = 300;

export function ChatHistory({ videoUuid, currentTimeMs }: ChatHistoryProps) {
    // 화면에 표시할 채팅
    const [displayedChats, setDisplayedChats] = useState<ChatHistoryResponse[]>([]);
    // 미리 불러온 채팅 버퍼
    const chatBufferRef = useRef<ChatHistoryResponse[]>([]);
    // 다음에 로딩해야하는 offset millis
    const nextLoadOffsetMillisRef = useRef(0);
    // 이전 시간
    const beforeTimeMillisRef = useRef(0);
    const lastChatRef = useRef<HTMLDivElement>(null);

    // 채팅 불러오기
    const loadChat = useCallback(async (offsetStart: number, timeMillis: number) => {
        const start = Math.floor(offsetStart);
        const end = Math.floor(offsetStart + timeMillis);
        try {
            const response = await getVideoChatHistory(videoUuid, { offsetStart: start, offsetEnd: end });
            chatBufferRef.current = [...chatBufferRef.current, ...response]
                .sort((a, b) => a.offsetMillis - b.offsetMillis);
            nextLoadOffsetMillisRef.current = end;
        } catch (error) {
            console.error('채팅 로딩 실패', error);
        }
    }, [videoUuid]);

    // 채팅 다시 불러오기
    const reloadChat = useCallback(async () => {
        setDisplayedChats([]);
        chatBufferRef.current = [];

        const offsetStart = Math.max(0, currentTimeMs - LOAD_BEFORE_CHAT_MILLIS);
        await loadChat(offsetStart, CHAT_LOAD_MILLIS + LOAD_BEFORE_CHAT_MILLIS);
        nextLoadOffsetMillisRef.current = currentTimeMs + CHAT_LOAD_MILLIS;
    }, [currentTimeMs, loadChat]);

    // 현재 시간까지의 채팅 표시
    const addChatBeforeTimeMillis = useCallback((timeMillis: number) => {
        const chatsToShow = chatBufferRef.current.filter(
            (chat) => chat.offsetMillis <= timeMillis
        );
        chatBufferRef.current = chatBufferRef.current.filter(
            (chat) => chat.offsetMillis > timeMillis
        );

        if (chatsToShow.length > 0) {
            setDisplayedChats((prev) => {
                const newChats = [...prev, ...chatsToShow];
                // 최대 개수 유지
                if (newChats.length > MAX_NUMBER_OF_CHAT) {
                    return newChats.slice(-MAX_NUMBER_OF_CHAT);
                }
                return newChats;
            });
        }
    }, []);

    // 시간 업데이트 시 처리
    useEffect(() => {
        const beforeTimeMillis = beforeTimeMillisRef.current;

        // 건너뛰었을 경우
        if (Math.abs(beforeTimeMillis - currentTimeMs) > 1000) {
            reloadChat();
        } else {
            // 현재 시간까지 채팅 표시
            addChatBeforeTimeMillis(currentTimeMs);

            // 다음 로딩 시점 근처에 도달하면 미리 로딩
            if (currentTimeMs > nextLoadOffsetMillisRef.current - (CHAT_LOAD_MILLIS / 2)) {
                loadChat(nextLoadOffsetMillisRef.current, CHAT_LOAD_MILLIS);
            }
        }

        beforeTimeMillisRef.current = currentTimeMs;
    }, [currentTimeMs, reloadChat, addChatBeforeTimeMillis, loadChat]);

    // 초기 로드
    useEffect(() => {
        reloadChat();
    }, [videoUuid]);

    // 새 채팅이 추가되면 스크롤 하단으로 이동
    useEffect(() => {
        if (lastChatRef.current) {
            lastChatRef.current.scrollIntoView({ block: 'end' });
        }
    }, [displayedChats]);

    return (
        <div className="h-full flex flex-col">
            <div className="hidden lg:flex px-4 py-3 mx-auto">
                <h2 className="font-semibold">채팅 기록</h2>
            </div>

            <Separator />

            {/* 채팅 목록 */}
            <div className="overflow-hidden flex-1">
                <ScrollArea className="h-full">
                    <div className="px-4 py-2 space-y-2 text-sm">
                        {displayedChats.map((chat, index) => {
                            const isLast = index === displayedChats.length - 1;
                            return (
                                <div
                                    key={`${chat.offsetMillis}-${index}`}
                                    className="leading-relaxed"
                                    ref={isLast ? lastChatRef : null}
                                >
                                    <span className="text-muted-foreground text-xs">[{formatTime(chat.offsetMillis)}]</span>{' '}
                                    <span className={`font-semibold ${getUsernameColor(chat.username)}`}>{chat.username}</span>:{' '}
                                    <span>{chat.message}</span>
                                </div>
                            );
                        })}
                    </div>
                </ScrollArea>
            </div>
        </div>
    );
}