import { VideoGridSkeleton } from '@/components/video-grid-skeleton';

export default function Loading() {
    return (
        <div className="p-4 md:p-6">
            <VideoGridSkeleton />
        </div>
    );
}
