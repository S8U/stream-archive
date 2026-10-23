import { LinkIcon } from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';

interface ChannelAvatarProps {
    channel: {
        name: string;
        profileUrl: string;
        contentPrivacy: 'PUBLIC' | 'UNLISTED' | 'PRIVATE';
    };
    /** 아바타 크기와 첫 글자 크기 (예: w-8 h-8, w-20 h-20 text-4xl) */
    className?: string;
}

// 채널 아바타
// 일부공개 채널이면 오른쪽 아래에 링크 아이콘 배지를 붙인다.
// 배지는 Avatar를 감싸는 요소에 둔다.
// Avatar는 원으로 잘려서, 안에 두면 원 밖으로 나간 부분이 잘린다.
// 감싸는 요소는 크기를 아바타에 맞춘다(size-fit).
// flex 줄에서 줄 높이만큼 늘어나면 배지가 아바타에서 떨어지기 때문이다.
export function ChannelAvatar({ channel, className }: ChannelAvatarProps) {
    return (
        <span className="relative grid size-fit shrink-0">
            <Avatar className={className}>
                <AvatarImage src={channel.profileUrl} />
                <AvatarFallback>{channel.name[0]}</AvatarFallback>
            </Avatar>
            {channel.contentPrivacy === 'UNLISTED' && (
                // 배지 가운데를 원 둘레의 오른쪽 아래(45도) 지점에 둔다.
                // 배지는 아바타 지름의 22%이고 14px보다 작아지지 않는다.
                // 그래서 사이드바의 작은 아바타와 채널 머리의 큰 아바타에 같은 모양으로 붙는다.
                <span className="absolute top-[85.4%] left-[85.4%] flex size-[max(14px,22%)] -translate-x-1/2 -translate-y-1/2 items-center justify-center rounded-full bg-background text-muted-foreground ring-1 ring-border">
                    <LinkIcon className="size-[70%]" role="img" aria-label="일부공개 채널" />
                </span>
            )}
        </span>
    );
}
