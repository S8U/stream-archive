'use client';

import { useEffect, useRef, useState, useCallback } from 'react';
import { Eye, EyeOff, MoreVertical } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from "@/components/ui/separator";
import { useGetVideoChatHistory } from '@/lib/api/endpoints/video/video';
import type { ChatHistoryResponse } from '@/lib/api/models/chatHistoryResponse';

interface ChatHistoryProps {
    videoUuid: string;
    currentTimeMs: number;
    chatSyncOffsetMillis: number;
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
const SHOW_TIMELINE_STORAGE_KEY = 'chat-history:show-timeline';

export function ChatHistory({ videoUuid, currentTimeMs: rawCurrentTimeMs, chatSyncOffsetMillis }: ChatHistoryProps) {
    // 싱크 오프셋 적용 (음수로 적용하여 채팅이 영상보다 앞서 표시되도록)
    const currentTimeMs = rawCurrentTimeMs - chatSyncOffsetMillis;
    // 화면에 표시할 채팅
    const [displayedChats, setDisplayedChats] = useState<ChatHistoryResponse[]>([]);
    // 미리 불러온 채팅 버퍼
    const chatBufferRef = useRef<ChatHistoryResponse[]>([]);
    // 다음에 로딩해야하는 offset millis
    const nextLoadOffsetMillisRef = useRef(0);
    // 이전 시간
    const beforeTimeMillisRef = useRef(0);
    const lastChatRef = useRef<HTMLDivElement>(null);
    const viewportRef = useRef<HTMLDivElement | null>(null);
    // 사용자가 맨 아래에 있는지 여부 (위로 올리면 자동 스크롤 중단)
    const isAtBottomRef = useRef(true);
    const [showTimeline, setShowTimeline] = useState(false);

    // API 요청 파라미터
    const [fetchParams, setFetchParams] = useState<{ offsetStart: number; offsetEnd: number } | null>(null);

    // 채팅 불러오기
    const { data } = useGetVideoChatHistory(
        videoUuid,
        fetchParams || { offsetStart: 0, offsetEnd: 0 },
        {
            query: {
                enabled: fetchParams !== null,
                refetchInterval: false,
            },
        }
    );

    // API 응답 처리
    useEffect(() => {
        if (data && fetchParams) {
            chatBufferRef.current = [...chatBufferRef.current, ...data]
                .sort((a, b) => a.offsetMillis - b.offsetMillis);
            nextLoadOffsetMillisRef.current = fetchParams.offsetEnd;
            setFetchParams(null); // 요청 완료
        }
    }, [data, fetchParams]);

    // 브라우저에 저장된 타임라인 표시 설정 불러오기
    useEffect(() => {
        setShowTimeline(localStorage.getItem(SHOW_TIMELINE_STORAGE_KEY) === 'true');
    }, []);

    const handleShowTimelineToggle = useCallback(() => {
        setShowTimeline((prev) => {
            const next = !prev;
            localStorage.setItem(SHOW_TIMELINE_STORAGE_KEY, String(next));
            return next;
        });
    }, []);

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

    // 채팅 불러오기
    const loadChat = useCallback((offsetStart: number, timeMillis: number) => {
        const start = Math.floor(offsetStart);
        const end = Math.floor(offsetStart + timeMillis);
        setFetchParams({ offsetStart: start, offsetEnd: end });
    }, []);

    // 채팅 다시 불러오기 (건너뛰기 시)
    const reloadChat = useCallback(() => {
        setDisplayedChats([]);
        chatBufferRef.current = [];
        // 건너뛰면 채팅도 현재 시점으로 따라가도록 자동 스크롤 복귀
        isAtBottomRef.current = true;

        const offsetStart = Math.max(0, currentTimeMs - LOAD_BEFORE_CHAT_MILLIS);
        loadChat(offsetStart, CHAT_LOAD_MILLIS + LOAD_BEFORE_CHAT_MILLIS);
        nextLoadOffsetMillisRef.current = currentTimeMs + CHAT_LOAD_MILLIS;
    }, [currentTimeMs, loadChat]);

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

    // 스크롤 위치 추적: 사용자가 맨 아래 근처에 있으면 자동 스크롤 유지, 위로 올리면 중단
    useEffect(() => {
        const viewport = viewportRef.current;
        if (!viewport) return;

        const BOTTOM_THRESHOLD_PX = 80;
        const handleScroll = () => {
            const distanceFromBottom =
                viewport.scrollHeight - viewport.scrollTop - viewport.clientHeight;
            isAtBottomRef.current = distanceFromBottom <= BOTTOM_THRESHOLD_PX;
        };

        viewport.addEventListener('scroll', handleScroll, { passive: true });
        return () => viewport.removeEventListener('scroll', handleScroll);
    }, []);

    // 새 채팅이 추가되면 스크롤 하단으로 이동 (사용자가 맨 아래에 있을 때만)
    useEffect(() => {
        if (isAtBottomRef.current && lastChatRef.current) {
            lastChatRef.current.scrollIntoView({ block: 'end' });
        }
    }, [displayedChats]);

    return (
        <div className="h-full flex flex-col">
            <div className="hidden lg:grid grid-cols-[1fr_auto_1fr] items-center px-4 py-2">
                <h2 className="col-start-2 font-semibold">채팅 기록</h2>
                <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                        <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            className="col-start-3 justify-self-end -mr-2 h-7 w-7"
                            aria-label="채팅 기록 설정"
                        >
                            <MoreVertical className="h-4 w-4" />
                        </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                        <DropdownMenuItem
                            onClick={handleShowTimelineToggle}
                        >
                            {showTimeline ? (
                                <>
                                    <EyeOff className="h-4 w-4" />
                                    타임라인 가리기
                                </>
                            ) : (
                                <>
                                    <Eye className="h-4 w-4" />
                                    타임라인 보기
                                </>
                            )}
                        </DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu>
            </div>

            <Separator />

            {/* 채팅 목록 */}
            <div className="overflow-hidden flex-1">
                <ScrollArea viewportRef={viewportRef} className="h-full">
                    <div className="px-4 py-2 space-y-2 text-sm">
                        {displayedChats.map((chat, index) => {
                            const isLast = index === displayedChats.length - 1;
                            return (
                                <div
                                    key={`${chat.offsetMillis}-${index}`}
                                    className="flex w-full flex-wrap items-baseline gap-x-2 leading-relaxed"
                                    ref={isLast ? lastChatRef : null}
                                >
                                    {showTimeline && (
                                        <>
                                            <span className="text-muted-foreground text-xs">[{formatTime(chat.offsetMillis + chatSyncOffsetMillis)}]</span>
                                        </>
                                    )}
                                    <span className={`font-semibold ${getUsernameColor(chat.username)}`}>{chat.username}</span>
                                    <span className="font-normal">{chat.message}</span>
                                </div>
                            );
                        })}
                    </div>
                </ScrollArea>
            </div>
        </div>
    );
}
