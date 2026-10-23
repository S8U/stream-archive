import { Bookmark, LinkIcon } from 'lucide-react';

interface VideoCardTitleProps {
    title: string;
    contentPrivacy: 'PUBLIC' | 'UNLISTED' | 'PRIVATE';
    isArchived?: boolean;
}

// 동영상 카드 제목
// 일부공개 동영상이면 링크 아이콘, 소장한 동영상이면 소장 아이콘을 제목 뒤에 붙인다.
// 아이콘은 제목 글자와 따로 둔다.
// 제목이 두 줄을 넘어 말줄임돼도 아이콘은 첫 줄 오른쪽에 남는다.
export function VideoCardTitle({ title, contentPrivacy, isArchived }: VideoCardTitleProps) {
    return (
        <h3 className="flex items-start gap-1 text-md font-medium">
            <span className="line-clamp-2">{title}</span>
            {(contentPrivacy === 'UNLISTED' || isArchived) && (
                <span className="flex h-6 shrink-0 items-center gap-1 text-muted-foreground">
                    {contentPrivacy === 'UNLISTED' && (
                        <LinkIcon size={13} role="img" aria-label="일부공개 동영상" />
                    )}
                    {isArchived && (
                        <Bookmark size={13} className="opacity-50" fill="currentColor" />
                    )}
                </span>
            )}
        </h3>
    );
}
