import { Link } from "react-router-dom";
import type {VideoListItem} from "../types/video.ts";
// import {formatDuration} from "../utils/formatDuration.ts";
import {formatViews} from "../utils/formatViews.ts";
import "../styles/video.css";

type Props = {
    video: VideoListItem;
};

export function VideoCard({ video }: Props) {
    return (
        <article className="video-card">
            <Link to={`/videos/${video.videoId}`} className="video-card__thumbnail-link">
                <div className="video-card__thumbnail-wrapper">
                    <img
                        src={"http://localhost:8080" + video.previewUrl}
                        alt={video.title}
                        className="video-card__thumbnail"
                        loading="lazy"
                    />
                    {/*<span className="video-card__duration">*/}
            {/*{formatDuration(100)}*/}
          {/*</span>*/}
                </div>
            </Link>

            <div className="video-card__body">
                <Link to={`/videos/${video.videoId}`} className="video-card__title-link">
                    <h3 className="video-card__title">{video.title}</h3>
                </Link>

                <p className="video-card__meta">
                    {video.author.displayName || video.author.username}
                </p>

                <p className="video-card__meta">
                    {formatViews(video.viewsCount)} views •{" "}
                </p>

                <p className="video-card__meta">
                    {new Date(video.createdAt).toLocaleDateString()}
                </p>

                <p className="video-card__meta">
                    {video.likesCount} likes • {video.commentsCount} comments
                </p>

                <p className="video-card__description">
                    {video.description}
                </p>
            </div>
        </article>
    );
}