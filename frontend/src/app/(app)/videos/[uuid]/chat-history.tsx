'use client';

import { useEffect, useRef, useState, useCallback } from 'react';
import { Crown, Eye, EyeOff, HandCoins, MoreVertical, Wrench, type LucideIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from "@/components/ui/separator";
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { useGetVideoChatHistory } from '@/lib/api/endpoints/video/video';
import type { VideoChatHistorySearchResponse } from '@/lib/api/models/videoChatHistorySearchResponse';
import { VideoChatHistorySearchResponseMessageType } from '@/lib/api/models/videoChatHistorySearchResponseMessageType';
import type { VideoChatHistorySearchResponseUserRole } from '@/lib/api/models/videoChatHistorySearchResponseUserRole';
import { cn } from '@/lib/utils';

interface ChatHistoryProps {
    videoUuid: string;
    currentTimeMs: number;
    chatSyncOffsetMillis: number;
}

// 응답에 채팅 고유 ID가 없어서 받을 때 번호를 붙인다
// 오래된 채팅을 지울 때 index를 key로 쓰면 남은 줄이 전부 다시 그려지므로 이 번호를 key로 쓴다
type DisplayChat = VideoChatHistorySearchResponse & { localId: number };

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

// 역할이 있는 작성자는 채팅이 빠르게 흘러도 눈에 띄도록 아이콘과 배경으로 구분한다
const USER_ROLE_DISPLAYS: Record<VideoChatHistorySearchResponseUserRole, { label: string; icon: LucideIcon }> = {
    STREAMER: { label: '스트리머', icon: Crown },
    MANAGER: { label: '매니저', icon: Wrench },
};

function ChatUsername({ chat, className }: { chat: VideoChatHistorySearchResponse; className?: string }) {
    const roleDisplay = chat.userRole ? USER_ROLE_DISPLAYS[chat.userRole] : null;

    return (
        <span className={cn('font-semibold', getUsernameColor(chat.username), className)}>
            {roleDisplay && (
                <Tooltip>
                    <TooltipTrigger asChild>
                        <roleDisplay.icon
                            className="mr-1 inline-block h-3.5 w-3.5 cursor-default align-[-2px] text-foreground"
                            aria-label={roleDisplay.label}
                        />
                    </TooltipTrigger>
                    <TooltipContent className="rounded-full">{roleDisplay.label}</TooltipContent>
                </Tooltip>
            )}
            {chat.username}
        </span>
    );
}

function escapeRegExp(value: string): string {
    return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function renderChatMessage(chat: VideoChatHistorySearchResponse) {
    if (!chat.emojis || chat.emojis.length === 0) {
        return chat.message;
    }

    const emojiMap = new Map(chat.emojis.map((emoji) => [emoji.placeholder, emoji]));
    const pattern = new RegExp(`(${chat.emojis.map((emoji) => escapeRegExp(emoji.placeholder)).join('|')})`, 'g');

    return chat.message.split(pattern).map((part, index) => {
        if (part === '') return null;

        const emoji = emojiMap.get(part);
        if (!emoji) return <span key={index}>{part}</span>;

        return (
            <img
                key={index}
                src={emoji.imageUrl}
                alt={emoji.placeholder}
                title={emoji.placeholder}
                className="mx-0.5 inline-block h-6 w-6 align-middle object-contain"
                loading="lazy"
                draggable={false}
                onError={(event) => {
                    event.currentTarget.style.display = 'none';
                }}
            />
        );
    });
}

// 한번에 불러올 채팅 시간 (ms)
const CHAT_LOAD_MILLIS = 3000;
// 건너뛰었을 때 불러올 이전 채팅 시간 (ms)
const LOAD_BEFORE_CHAT_MILLIS = 60000;
// 채팅 최대 개수
const MAX_NUMBER_OF_CHAT = 300;
// 위로 올려 보는 동안 쌓아 둘 최대 채팅 수
// 끝없이 쌓이면 휴대폰에서 버벅이므로 이만큼 차면 새 채팅은 맨 아래로 돌아올 때까지 버퍼에 남겨 둔다
const MAX_NUMBER_OF_CHAT_WHILE_SCROLLED_UP = 1000;
// 재생 중 timeupdate 간격 흔들림을 시킹으로 오판하지 않기 위한 기준
const SEEK_RELOAD_THRESHOLD_MILLIS = 1500;
const SHOW_TIMELINE_STORAGE_KEY = 'chat-history:show-timeline';

export function ChatHistory({ videoUuid, currentTimeMs: rawCurrentTimeMs, chatSyncOffsetMillis }: ChatHistoryProps) {
    // 싱크 오프셋 적용 (음수로 적용하여 채팅이 영상보다 앞서 표시되도록)
    const currentTimeMs = rawCurrentTimeMs - chatSyncOffsetMillis;
    // 화면에 표시할 채팅
    const [displayedChats, setDisplayedChats] = useState<DisplayChat[]>([]);
    // 화면에 표시 중인 채팅 수 (시간 업데이트 콜백에서 읽는다)
    const displayedChatCountRef = useRef(0);
    // 미리 불러온 채팅 버퍼
    const chatBufferRef = useRef<DisplayChat[]>([]);
    // 다음에 받은 채팅에 붙일 번호
    const nextLocalIdRef = useRef(0);
    // 다음에 로딩해야하는 offset millis
    const nextLoadOffsetMillisRef = useRef(0);
    // 이전 시간
    const beforeTimeMillisRef = useRef(0);
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
            const receivedChats = data.map((chat) => ({ ...chat, localId: nextLocalIdRef.current++ }));
            chatBufferRef.current = [...chatBufferRef.current, ...receivedChats]
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
        const isAtBottom = isAtBottomRef.current;
        if (!isAtBottom && displayedChatCountRef.current >= MAX_NUMBER_OF_CHAT_WHILE_SCROLLED_UP) {
            // 맨 아래로 돌아오면 최근 MAX_NUMBER_OF_CHAT개만 남으므로 시간이 지난 채팅도 그만큼만 버퍼에 둔다
            // 다 남기면 올려 둔 시간만큼 버퍼가 끝없이 커진다
            const dueCount = chatBufferRef.current.filter(
                (chat) => chat.offsetMillis <= timeMillis
            ).length;
            if (dueCount > MAX_NUMBER_OF_CHAT) {
                chatBufferRef.current = chatBufferRef.current.slice(dueCount - MAX_NUMBER_OF_CHAT);
            }
            return;
        }

        const chatsToShow = chatBufferRef.current.filter(
            (chat) => chat.offsetMillis <= timeMillis
        );
        chatBufferRef.current = chatBufferRef.current.filter(
            (chat) => chat.offsetMillis > timeMillis
        );

        if (chatsToShow.length > 0) {
            setDisplayedChats((prev) => {
                const newChats = [...prev, ...chatsToShow];
                // 최대 개수는 맨 아래에 있을 때만 맞춘다
                // 위로 올려 보는 동안 위쪽 채팅을 지우면 보던 채팅이 밀려 올라간다
                if (isAtBottom && newChats.length > MAX_NUMBER_OF_CHAT) {
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
        if (Math.abs(beforeTimeMillis - currentTimeMs) > SEEK_RELOAD_THRESHOLD_MILLIS) {
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

    // 스크롤 위치 추적: 사용자가 맨 아래에 있으면 자동 스크롤 유지, 위로 올리면 중단
    useEffect(() => {
        const viewport = viewportRef.current;
        if (!viewport) return;

        // 소수점 스크롤 오차만 맨 아래로 본다
        // 조금이라도 올리면 그 자리를 유지한다
        const BOTTOM_THRESHOLD_PX = 4;
        const handleScroll = () => {
            const distanceFromBottom =
                viewport.scrollHeight - viewport.scrollTop - viewport.clientHeight;
            isAtBottomRef.current = distanceFromBottom <= BOTTOM_THRESHOLD_PX;
        };

        viewport.addEventListener('scroll', handleScroll, { passive: true });
        return () => viewport.removeEventListener('scroll', handleScroll);
    }, []);

    // 표시 중인 채팅 수를 ref에 맞춘다
    useEffect(() => {
        displayedChatCountRef.current = displayedChats.length;
    }, [displayedChats]);

    // 새 채팅이 추가되면 스크롤 하단으로 이동 (사용자가 맨 아래에 있을 때만)
    // scrollIntoView는 페이지 같은 바깥 스크롤까지 움직이므로 채팅창만 직접 내린다
    useEffect(() => {
        const viewport = viewportRef.current;
        if (isAtBottomRef.current && viewport) {
            viewport.scrollTop = viewport.scrollHeight;
        }
    }, [displayedChats]);

    return (
        <div className="h-full flex flex-col">
            <div className="hidden lg:grid grid-cols-[1fr_auto_1fr] items-center px-4 py-2">
                <h2 className="col-start-2 font-semibold">채팅</h2>
                <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                        <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            className="col-start-3 justify-self-end -mr-2 h-7 w-7"
                            aria-label="채팅 설정"
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
                <ScrollArea viewportRef={viewportRef} className="h-full" viewportClassName="overscroll-contain" hideScrollbar>
                    <div className="px-4 py-2 space-y-2 text-sm">
                        {displayedChats.map((chat) => {
                            // 채팅 한 줄은 flex가 아닌 인라인 흐름으로 쌓는다
                            // flex 아이템이면 긴 메시지가 닉네임 옆에 다 들어가지 않을 때 통째로 다음 줄로 넘어가기 때문이다
                            const timeline = showTimeline && (
                                <span className="mr-2 text-muted-foreground text-xs">
                                    [{formatTime(chat.offsetMillis + chatSyncOffsetMillis)}]
                                </span>
                            );

                            if (chat.messageType === VideoChatHistorySearchResponseMessageType.DONATION) {
                                // 치지직 채팅창과 같은 순서로 닉네임, 메시지, 금액을 한 줄씩 쌓는다
                                // 지금은 치지직 후원만 저장하므로 금액 단위는 치즈다
                                return (
                                    <div
                                        key={chat.localId}
                                        className="-mx-2 rounded-xl bg-muted px-3 py-2 leading-relaxed"
                                    >
                                        <div>
                                            {timeline}
                                            <ChatUsername chat={chat} />
                                        </div>
                                        {chat.message && (
                                            <div className="break-words [overflow-wrap:anywhere]">
                                                {renderChatMessage(chat)}
                                            </div>
                                        )}
                                        <div className="mt-1.5 inline-flex items-center gap-1 rounded-full bg-background px-2.5 py-0.5 font-semibold">
                                            <HandCoins className="h-4 w-4" aria-hidden />
                                            {(chat.donationAmount ?? 0).toLocaleString()}치즈
                                        </div>
                                    </div>
                                );
                            }

                            const roleDisplay = chat.userRole ? USER_ROLE_DISPLAYS[chat.userRole] : null;
                            return (
                                <div
                                    key={chat.localId}
                                    className={cn(
                                        'leading-relaxed',
                                        // 배경을 글자 바깥으로 넓혀도 글자 위치는 다른 채팅과 맞춘다
                                        roleDisplay && '-mx-2 rounded-lg bg-muted px-2 py-1'
                                    )}
                                >
                                    {timeline}
                                    <ChatUsername chat={chat} className="mr-2" />
                                    <span className="font-normal break-words [overflow-wrap:anywhere]">
                                        {renderChatMessage(chat)}
                                    </span>
                                </div>
                            );
                        })}
                    </div>
                </ScrollArea>
            </div>
        </div>
    );
}
