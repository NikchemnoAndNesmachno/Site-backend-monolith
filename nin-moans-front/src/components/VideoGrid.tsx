import { VideoCard } from "./VideoCard";
import type { VideoListItem } from "../types/video.ts";

type Props = {
    videos: VideoListItem[];
};

export function VideoGrid({ videos }: Props) {
    if (videos.length === 0) {
        return (
            <div className="video-grid video-grid--empty">
                <p className="video-grid__empty-title">Відео поки не знайдено.</p>
                <p className="video-grid__empty-description">
                    Спробуйте переключити сортування.
                </p>
            </div>
        );
    }

    return (
        <section className="video-grid">
            {videos.map((video) => (
                <VideoCard key={video.videoId} video={video} />
            ))}
        </section>
    );
}