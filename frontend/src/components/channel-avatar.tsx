import { LinkIcon } from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';

interface ChannelAvatarProps {
    channel: {
        name: string;
        profileUrl: string;
        contentPrivacy: 'PUBLIC' | 'UNLISTED' | 'PRIVATE';
    };
    /** 아바타 크기 (예: w-8 h-8) */
    className?: string;
}

// 채널 아바타
// 일부공개 채널이면 오른쪽 아래에 링크 아이콘 배지를 붙인다.
// 배지는 Avatar를 감싸는 요소에 둔다.
// Avatar는 원으로 잘려서, 안에 두면 원 밖으로 나간 부분이 잘린다.
export function ChannelAvatar({ channel, className }: ChannelAvatarProps) {
    return (
        <span className="relative grid shrink-0">
            <Avatar className={className}>
                <AvatarImage src={channel.profileUrl} />
                <AvatarFallback>{channel.name[0]}</AvatarFallback>
            </Avatar>
            {/* 관리자가 일부공개 채널 보기를 켰을 때만 일부공개 채널이 목록에 나온다. */}
            {channel.contentPrivacy === 'UNLISTED' && (
                <span className="absolute -right-0.5 -bottom-0.5 flex size-3.5 items-center justify-center rounded-full bg-background text-muted-foreground ring-1 ring-border">
                    <LinkIcon className="size-2.5" role="img" aria-label="일부공개 채널" />
                </span>
            )}
        </span>
    );
}
