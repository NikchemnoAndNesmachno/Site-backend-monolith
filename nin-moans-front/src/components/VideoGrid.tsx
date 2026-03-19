import { VideoCard } from "./VideoCard";
import type {VideoListItem} from "../types/video.ts";

type Props = {
    videos: VideoListItem[];
};

export function VideoGrid({ videos }: Props) {
    if (videos.length === 0) {
        return <p>No videos found.</p>;
    }

    return (
        <section className="video-grid">
            {videos.map((video) => (
                <VideoCard key={video.videoId} video={video} />
            ))}
        </section>
    );
}