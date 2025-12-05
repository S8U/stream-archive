'use client';

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import type { PublicChannelResponse } from '@/lib/api/models';

interface ChannelHeaderProps {
    channel: PublicChannelResponse;
}

export function ChannelHeader({ channel }: ChannelHeaderProps) {
    const formattedDate = new Date(channel.createdAt).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
    });

    return (
        <div className="flex items-center gap-6">
            <Avatar className="w-32 h-32">
                <AvatarImage src={channel.profileUrl} />
                <AvatarFallback className="text-4xl">{channel.name[0]}</AvatarFallback>
            </Avatar>
            <div>
                <h1 className="text-3xl font-bold mb-2">{channel.name}</h1>
                <p className="text-muted-foreground">{formattedDate}부터 활동</p>
            </div>
        </div>
    );
}