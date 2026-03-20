import { Link } from "react-router-dom";
import type {VideoListItem} from "../types/video.ts";
// import {formatDuration} from "../utils/formatDuration.ts";
import {formatViews} from "../utils/formatViews.ts";
import "../styles/video.css";
import {BASE_URL} from "../api/axios.ts";

type Props = {
    video: VideoListItem;
};

export function VideoCard({ video }: Props) {
    const authorName = video.author.displayName || video.author.username;
    const createdAt = new Date(video.createdAt);
    const formattedDate = createdAt.toLocaleDateString();
    const description = video.description?.trim() || "Опис відсутній.";
    const avatarLetter = authorName.charAt(0).toUpperCase();
    const avatarUrl = video.author.avatarUrl ? `${BASE_URL}${video.author.avatarUrl}` : null;

    return (
        <article className="video-card">
            <Link to={`/videos/${video.videoId}`} className="video-card__thumbnail-link">
                <div className="video-card__thumbnail-wrapper">
                    <img
                        src={`${BASE_URL}${video.previewUrl}`}
                        alt={video.title}
                        className="video-card__thumbnail"
                        loading="lazy"
                    />
                    {/*<span className="video-card__duration">*/}
            {/*{formatDuration(100)}*/}
          {/*</span>*/}
                    <div className="video-card__overlay">
                        <span className="video-card__chip">{formatViews(video.viewsCount)} views</span>
                        <span className="video-card__chip video-card__chip--muted">
                            {video.likesCount} likes
                        </span>
                    </div>
                </div>
            </Link>

            <div className="video-card__body">
                <div className="video-card__author-row">
                    <div className="video-card__avatar">
                        {avatarUrl ? (
                            <img src={avatarUrl} alt={authorName} className="video-card__avatar-image" />
                        ) : (
                            <span>{avatarLetter}</span>
                        )}
                    </div>

                    <div className="video-card__author-meta">
                        <span className="video-card__author-name">{authorName}</span>
                        <time className="video-card__date" dateTime={video.createdAt}>
                            {formattedDate}
                        </time>
                    </div>
                </div>

                <Link to={`/videos/${video.videoId}`} className="video-card__title-link">
                    <h3 className="video-card__title">{video.title}</h3>
                </Link>

                <p className="video-card__description">{description}</p>

                <div className="video-card__stats-row" aria-label="video stats">
                    <span className="video-card__stat">👁 {formatViews(video.viewsCount)}</span>
                    <span className="video-card__stat">💬 {video.commentsCount}</span>
                    <span className="video-card__stat">👍 {video.likesCount}</span>
                </div>
            </div>
        </article>
    );
}