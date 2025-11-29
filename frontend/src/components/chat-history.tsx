'use client';

import { ScrollArea } from '@/components/ui/scroll-area';
import {Separator} from "@/components/ui/separator";

interface ChatHistoryProps {
    videoUuid: string;
}

interface ChatMessage {
    id: number;
    username: string;
    message: string;
    timestamp: number; // milliseconds
}

// 더미 채팅 데이터
const DUMMY_CHATS: ChatMessage[] = [
    { id: 1, username: '시청자123', message: '안녕하세요!', timestamp: 0 },
    { id: 2, username: '게이머999', message: 'ㅋㅋㅋㅋㅋ', timestamp: 3000 },
    { id: 3, username: '방송러버', message: '오늘 방송 재밌네요', timestamp: 5500 },
    { id: 4, username: '시청자123', message: '구독 좋아요 알람 설정!!!', timestamp: 8000 },
    { id: 5, username: '익명의시청자', message: '이거 어떻게 하는거에요?', timestamp: 12000 },
    { id: 6, username: '게이머999', message: '클릭하면 됩니다', timestamp: 15000 },
    { id: 7, username: '프로게이머', message: '와 대박', timestamp: 20000 },
    { id: 8, username: '방송러버', message: 'ㅇㅈ', timestamp: 22000 },
    { id: 9, username: '시청자456', message: '오늘도 화이팅~!', timestamp: 25000 },
    { id: 10, username: '익명의시청자', message: '감사합니다', timestamp: 28000 },
    { id: 11, username: 'VIP멤버', message: '후원 10만원 보냅니다', timestamp: 32000 },
    { id: 12, username: '시청자123', message: 'ㄷㄷㄷ 부럽', timestamp: 33000 },
    { id: 13, username: '게이머999', message: '오 감사합니다!', timestamp: 35000 },
    { id: 14, username: '뉴비', message: '처음 왔는데 분위기 좋네요', timestamp: 40000 },
    { id: 15, username: '방송러버', message: '환영합니다~~', timestamp: 42000 },
    { id: 16, username: '프로게이머', message: 'gg', timestamp: 50000 },
    { id: 17, username: '시청자789', message: '클립 ㄱㄱ', timestamp: 52000 },
    { id: 18, username: '게이머999', message: '이 장면은 레전드', timestamp: 55000 },
    { id: 19, username: '시청자123', message: '진짜 인정', timestamp: 57000 },
    { id: 20, username: '익명의시청자', message: '다시보기로 또 볼듯', timestamp: 60000 },
    { id: 21, username: '팬1', message: '오늘 컨디션 좋으시네요', timestamp: 65000 },
    { id: 22, username: '방송러버', message: '인정합니다', timestamp: 67000 },
    { id: 23, username: '게이머999', message: '역시 프로답다', timestamp: 70000 },
    { id: 24, username: '시청자123', message: '이게 실화냐', timestamp: 72000 },
    { id: 25, username: '뉴비', message: '와...', timestamp: 75000 },
    { id: 26, username: 'VIP멤버', message: '클립 저장했습니다', timestamp: 78000 },
    { id: 27, username: '프로게이머', message: '이건 진짜 신의 한수', timestamp: 80000 },
    { id: 28, username: '시청자456', message: 'ㅁㅊ', timestamp: 82000 },
    { id: 29, username: '익명의시청자', message: '소름돋네', timestamp: 85000 },
    { id: 30, username: '방송러버', message: '이 정도면 명장면이죠', timestamp: 88000 },
    { id: 31, username: '게이머999', message: '유튜브 편집본 기대합니다', timestamp: 90000 },
    { id: 32, username: '시청자123', message: '편집자님 일 생겼다', timestamp: 92000 },
    { id: 33, username: '팬1', message: 'ㅋㅋㅋㅋㅋㅋㅋ', timestamp: 95000 },
    { id: 34, username: '뉴비', message: '구독 눌렀습니다', timestamp: 98000 },
    { id: 35, username: '방송러버', message: '좋아요도 눌러주세요~', timestamp: 100000 },
    { id: 36, username: 'VIP멤버', message: '알림 설정 필수', timestamp: 102000 },
    { id: 37, username: '프로게이머', message: '다음 방송 언제 하세요?', timestamp: 105000 },
    { id: 38, username: '시청자456', message: '내일 같은 시간이래요', timestamp: 107000 },
    { id: 39, username: '게이머999', message: '오 감사합니다', timestamp: 110000 },
    { id: 40, username: '익명의시청자', message: '기다리겠습니다', timestamp: 112000 },
    { id: 41, username: '시청자123', message: '오늘 하이라이트 언제 올라오나요?', timestamp: 115000 },
    { id: 42, username: '팬1', message: '보통 다음날 오후쯤이요', timestamp: 118000 },
    { id: 43, username: '방송러버', message: '편집 퀄리티가 좋아서 시간 좀 걸림', timestamp: 120000 },
    { id: 44, username: '뉴비', message: '그럼 내일 확인해볼게요', timestamp: 123000 },
    { id: 45, username: 'VIP멤버', message: '디스코드에도 올라옵니다', timestamp: 125000 },
    { id: 46, username: '게이머999', message: '디코 링크 어디있나요?', timestamp: 128000 },
    { id: 47, username: '프로게이머', message: '방송 소개란에 있어요', timestamp: 130000 },
    { id: 48, username: '시청자456', message: '찾았습니다 감사합니다', timestamp: 132000 },
    { id: 49, username: '방송러버', message: '여기 분위기 진짜 좋다', timestamp: 135000 },
    { id: 50, username: '익명의시청자', message: '인정이요', timestamp: 137000 },
    { id: 51, username: '시청자123', message: '다들 착하심', timestamp: 140000 },
    { id: 52, username: '팬1', message: '스트리머분도 친절하시고', timestamp: 142000 },
    { id: 53, username: '뉴비', message: '처음인데 벌써 좋아졌어요', timestamp: 145000 },
    { id: 54, username: 'VIP멤버', message: '환영합니다~!', timestamp: 147000 },
    { id: 55, username: '게이머999', message: '자주 놀러오세요', timestamp: 150000 },
    { id: 56, username: '프로게이머', message: '다음 게임 뭐 하시나요?', timestamp: 153000 },
    { id: 57, username: '시청자456', message: '투표 진행 중이래요', timestamp: 155000 },
    { id: 58, username: '방송러버', message: '저는 RPG 했으면 좋겠어요', timestamp: 158000 },
    { id: 59, username: '익명의시청자', message: '저는 FPS!', timestamp: 160000 },
    { id: 60, username: '시청자123', message: '공포게임도 재밌을 듯', timestamp: 163000 },
    { id: 61, username: '팬1', message: '공포는 리액션이 예술이죠', timestamp: 165000 },
    { id: 62, username: '뉴비', message: 'ㅋㅋㅋ맞아요', timestamp: 168000 },
    { id: 63, username: 'VIP멤버', message: '지난번에 진짜 웃겼음', timestamp: 170000 },
    { id: 64, username: '게이머999', message: '그 비명소리 ㅋㅋㅋ', timestamp: 173000 },
    { id: 65, username: '프로게이머', message: '클립으로 봤는데 레전드더라', timestamp: 175000 },
    { id: 66, username: '시청자456', message: '다시 봐도 웃김', timestamp: 178000 },
    { id: 67, username: '방송러버', message: '저도 그거 5번은 봤어요', timestamp: 180000 },
    { id: 68, username: '익명의시청자', message: '링크 부탁드려요', timestamp: 183000 },
    { id: 69, username: '시청자123', message: '채팅창에 못 올려요', timestamp: 185000 },
    { id: 70, username: '팬1', message: '하이라이트 영상에 있습니다', timestamp: 188000 },
    { id: 71, username: '뉴비', message: '찾아볼게요 감사합니다', timestamp: 190000 },
    { id: 72, username: 'VIP멤버', message: '조회수 100만 넘은 그거', timestamp: 193000 },
    { id: 73, username: '게이머999', message: '아 그거구나', timestamp: 195000 },
    { id: 74, username: '프로게이머', message: '그거 진짜 명장면', timestamp: 198000 },
    { id: 75, username: '시청자456', message: '댓글도 재밌음', timestamp: 200000 },
];


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

export function ChatHistory({ videoUuid }: ChatHistoryProps) {
    // TODO: 채팅 API 연결 필요
    // const { data: chats, isLoading } = useGetChatHistory(videoUuid);

    return (
        <div className="h-full flex flex-col">
            <div className="hidden lg:flex px-4 py-3 mx-auto">
                <h2 className="font-semibold">채팅 기록</h2>
            </div>

            <Separator />

            {/* 채팅 목록 */}
            <div className="overflow-hidden">
                <ScrollArea className="h-full">
                    <div className="px-4 py-2 space-y-2 text-sm">
                        {DUMMY_CHATS.map((chat) => (
                            <div key={chat.id} className="leading-relaxed">
                                <span className="text-muted-foreground text-xs">[{formatTime(chat.timestamp)}]</span>{' '}
                                <span className="font-semibold">{chat.username}</span>:{' '}
                                <span>{chat.message}</span>
                            </div>
                        ))}
                    </div>
                </ScrollArea>
            </div>
        </div>
    );
}